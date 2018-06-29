package pers.cz.autoapidoc.interceptor;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import pers.cz.autoapidoc.common.ParamInfo;

import java.lang.reflect.Method;
import java.util.List;

public class ParamInterceptorImpl extends AnalyzeParam implements MethodInterceptor {
    private List<ParamInfo> requireParams;

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        //requireTypes
        String[] split = method.getName().split("(?i)get", 2);
        Class<?> type = method.getReturnType();
        if (split.length == 2) {
            char[] chars = split[1].toCharArray();
            chars[0] += 32;
            boolean recorded = false;
            for (ParamInfo requireParam : requireParams)
                if (requireParam.getName(false).equals(String.valueOf(chars))) {
                    recorded = true;
                    break;
                }
            if (!recorded)
                reflectParams(type, String.valueOf(chars), requireParams);
        }
        return methodProxy.invokeSuper(object, args);
    }

    public List<ParamInfo> getRequireParams() {
        return requireParams;
    }

    public void setRequireParams(List<ParamInfo> requireParams) {
        this.requireParams = requireParams;
    }

}
