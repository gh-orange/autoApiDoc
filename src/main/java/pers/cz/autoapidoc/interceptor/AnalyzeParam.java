package pers.cz.autoapidoc.interceptor;

import pers.cz.autoapidoc.common.ParamInfo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

abstract class AnalyzeParam {
    private static final List<Class> primitiveTypes = Arrays.asList(Date.class, String.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class);
    private Stack<Type> typeQueue = new Stack<>();
    protected StringBuilder stringBuilder = new StringBuilder();

    void reflectParams(Type requireType, String requireName, List<ParamInfo> requireParams) throws ClassNotFoundException {
        // ignored <>
        int actualTypeArgumentsLength = 0;
        try {
            ParameterizedType parameterizedType = (ParameterizedType) requireType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            actualTypeArgumentsLength = actualTypeArguments.length;
            typeQueue.addAll(Arrays.asList(actualTypeArguments));
            requireType = parameterizedType.getRawType();
        } catch (Exception ignored) {
        }
        // judge Bean
        Class<?> requireClazz;
        try {
            requireClazz = Class.forName(requireType.getTypeName());
        } catch (Exception e) {
            requireClazz = (Class<?>) requireType;
        }
        if (!requireClazz.isPrimitive())
            if (!primitiveTypes.contains(requireType))
                if (!Enum.class.isAssignableFrom((Class<?>) requireType)) {
                    if (Collection.class.isAssignableFrom((Class<?>) requireType) || Map.class.isAssignableFrom((Class<?>) requireType)) {
                        stringBuilder.append("\"").append(requireName).append("\":");
                        stringBuilder.append("[\n");
                        for (int i = 0; i < actualTypeArgumentsLength; i++) {
                            Type type = typeQueue.pop();
                            // judge T
                            if (type.getTypeName().equals("T"))
                                reflectParams(typeQueue.pop(), String.valueOf(i + 1), requireParams);
                            else
                                reflectParams(type, String.valueOf(i + 1), requireParams);
                        }
                        stringBuilder.append("],\n");
                    } else {
                        if (!requireName.isEmpty())
                            stringBuilder.append("\"").append(requireName).append("\":");
                        stringBuilder.append("{\n");
                        for (Field field : ((Class<?>) requireType).getDeclaredFields()) {
//                    ParamInfo paramInfo = new ParamInfo(field.getType().getSimpleName(), field.getName());
//                    requireParams.add(paramInfo);
//                    stringBuilder.append("\"").append(paramInfo.getName(false)).append("\":\"").append(paramInfo.getClazz()).append("\",\n");
                            Type fieldType;
                            // judge T
                            if (field.getGenericType().getTypeName().equals("T"))
                                fieldType = typeQueue.pop();
                            else
                                fieldType = field.getGenericType();
                            reflectParams(fieldType, field.getName(), requireParams);
                        }
                        stringBuilder.append("},\n");
                    }
                    return;
                }
        // judge java lang
        ParamInfo paramInfo = new ParamInfo(requireType.getTypeName(), requireName);
        requireParams.add(paramInfo);
        if (!paramInfo.getName(false).isEmpty())
            stringBuilder.append("\"").append(paramInfo.getName(false)).append("\":\"").append(paramInfo.getClazz()).append("\",\n");
    }

    public String getJSONString() {
        return stringBuilder.toString();
    }

}
