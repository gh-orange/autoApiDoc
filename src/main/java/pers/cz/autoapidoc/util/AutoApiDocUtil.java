package pers.cz.autoapidoc.util;

import net.sf.cglib.proxy.Enhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pers.cz.autoapidoc.common.ParamInfo;
import pers.cz.autoapidoc.common.PathType;
import pers.cz.autoapidoc.interceptor.ParamInterceptorImpl;
import pers.cz.autoapidoc.interceptor.ReturnInterceptorImpl;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AutoApiDocUtil {

    private ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private List<Class> scanAllClassInPackage(String packagePath, PathType pathType) throws ClassNotFoundException {
        List<Class> result = new ArrayList<>();
        switch (pathType) {
            //按类路径
            case SrcClassPath:
            case TestClassPath:
                result.add(Class.forName(packagePath));
                break;
            //按包路径
            case PackagePath:
                String packageDirName = packagePath.replace('.', '/');
                Enumeration<URL> dirs;
                try {
                    dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
                    while (dirs.hasMoreElements()) {
                        URL url = dirs.nextElement();
                        String protocol = url.getProtocol();
                        if ("file".equals(protocol)) {
                            String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                            result = findClassInPackageByFile(packagePath, filePath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return result;
    }

    private List<Class> findClassInPackageByFile(String packageName, String filePath) {
        ArrayList<Class> clazzList = new ArrayList<>();
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory())
            return clazzList;
        File[] dirFiles = dir.listFiles(file -> {
            boolean acceptDir = file.isDirectory();
            boolean acceptClass = file.getName().endsWith("class");
            return acceptDir || acceptClass;
        });
        if (dirFiles != null)
            for (File file : dirFiles)
                if (file.isDirectory())
                    clazzList.addAll(findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath()));
                else
                    try {
                        String className = file.getName().substring(0, file.getName().length() - 6);
                        clazzList.add(Class.forName(packageName + "." + className));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
        return clazzList;
    }

    public static Field[] getAllFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    public void createApiDoc(String packagePath, PathType pathType) {
        try {
            ParamInterceptorImpl paramInterceptor = new ParamInterceptorImpl();
            ReturnInterceptorImpl returnInterceptor = new ReturnInterceptorImpl();
            for (Class<?> clazz : scanAllClassInPackage(packagePath, pathType)) {
                //===============================================================
                // 读取源码文件
                String sourcePath = clazz.getResource("").getPath() + clazz.getSimpleName();
                // 源码路径，暂时写死
                String codePathType;
                switch (pathType) {
                    default:
                    case SrcClassPath:
                        codePathType = "main";
                        break;
                    case TestClassPath:
                        codePathType = "test";
                        break;
                }
                sourcePath = sourcePath.replaceAll("target", "src").replaceAll("test-classes", codePathType + "/java");
                BufferedReader reader = new BufferedReader(new FileReader(sourcePath + ".java"));
                StringBuilder sourceCode = new StringBuilder();
                reader.lines().forEach(line -> sourceCode.append(line).append("\n"));
                reader.close();
                //===============================================================
                // 执行注入流程
                RequestMapping clazzAnnotation = clazz.getAnnotation(RequestMapping.class);
                for (Method method : clazz.getMethods()) {
                    method.setAccessible(true);
                    returnInterceptor.setReturnParams(new ArrayList<>());
                    paramInterceptor.setRequireParams(new ArrayList<>());
                    RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                    GetMapping getMethodAnnotation = method.getAnnotation(GetMapping.class);
                    PostMapping postMethodAnnotation = method.getAnnotation(PostMapping.class);
                    if (null != methodAnnotation || null != getMethodAnnotation || null != postMethodAnnotation) {
                        // 暂时留坑，再写下去就成json生成器了...
                        StringBuilder remark = new StringBuilder();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] params = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            params[i] = Enhancer.create(parameterTypes[i], paramInterceptor);
                            for (Field field : getAllFields(parameterTypes[i])) {
                                try {
                                    field.setAccessible(true);
                                    field.set(params[i], field.getType().newInstance());
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        Object object = Enhancer.create(clazz, returnInterceptor);
                        for (Field field : getAllFields(clazz)) {
                            try {
                                field.setAccessible(true);
                                field.set(object, applicationContext.getBean(field.getType()));
                            } catch (Exception ignored) {
                            }
                        }
                        method.invoke(object, params);
                        RequestMethod[] methodRequestMethods = new RequestMethod[0];
                        if (null != methodAnnotation)
                            methodRequestMethods = methodAnnotation.method();
                        if (null != getMethodAnnotation)
                            methodRequestMethods = new RequestMethod[]{RequestMethod.GET};
                        if (null != postMethodAnnotation)
                            methodRequestMethods = new RequestMethod[]{RequestMethod.POST};
                        if (0 == methodRequestMethods.length)
                            methodRequestMethods = RequestMethod.values();
                        for (RequestMethod methodRequestMethod : methodRequestMethods)
                            for (String clazzRequestUri : clazzAnnotation.value()) {
                                String[] values = new String[0];
                                if (null != methodAnnotation)
                                    values = methodAnnotation.value();
                                if (null != getMethodAnnotation)
                                    values = getMethodAnnotation.value();
                                if (null != postMethodAnnotation)
                                    values = postMethodAnnotation.value();
                                for (String methodRequestUri : values) {
                                    remark.append("/**\n");
                                    remark.append("@api ");
                                    remark.append("{").append(methodRequestMethod.name()).append("} ");
                                    remark.append(clazzRequestUri).append(methodRequestUri);
                                    remark.append("\n");
                                    remark.append("@apiName ");
                                    remark.append("\n");
                                    remark.append("@apiGroup ");
                                    remark.append(clazzAnnotation.value()[0]);
                                    remark.append("\n");
                                    for (ParamInfo requestParam : paramInterceptor.getRequireParams()) {
                                        remark.append("@apiParam ");
                                        remark.append("{").append(requestParam.getClazz()).append("} ");
                                        remark.append(requestParam.getName());
                                        remark.append("\n");
                                    }
                                    for (ParamInfo requireParam : returnInterceptor.getReturnParams()) {
                                        remark.append("@apiSuccess ");
                                        remark.append("{").append(requireParam.getClazz()).append("} ");
                                        remark.append(requireParam.getName());
                                        remark.append("\n");
                                    }
                                    remark.append("@apiSuccessExample  Success-Response:");
                                    remark.append("\n");
                                    remark.append(returnInterceptor.getJSONString());
                                    remark.append("@apiErrorExample  Error-Response:");
                                    remark.append("\n");
                                    remark.append(returnInterceptor.getJSONString());
                                    remark.append("**/\n");
                                }
                            }
                        //添加注释
                        String annotationName = "";
                        if (null != methodAnnotation)
                            annotationName = RequestMapping.class.getSimpleName();
                        if (null != getMethodAnnotation)
                            annotationName = GetMapping.class.getSimpleName();
                        if (null != postMethodAnnotation)
                            annotationName = PostMapping.class.getSimpleName();
                        Matcher matcher = Pattern.compile("[ \\t]*?@.*?" + annotationName + ".*?\\s*?(private|protected|public)\\s+?.+?\\s+?" + method.getName() + "\\(").matcher(sourceCode.toString());
                        if (matcher.find())
                            sourceCode.insert(matcher.start(), remark.toString());
                    }
                }
                //===============================================================
                // 写出新文件
                String newResourceCode = sourceCode.toString();
                BufferedWriter writer = new BufferedWriter(new FileWriter(sourcePath + "-autoApiDoc.java"));
                writer.write(newResourceCode);
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
