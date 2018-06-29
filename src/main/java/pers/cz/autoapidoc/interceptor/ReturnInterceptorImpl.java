package pers.cz.autoapidoc.interceptor;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import pers.cz.autoapidoc.common.ParamInfo;

import java.lang.reflect.Method;
import java.util.List;

public class ReturnInterceptorImpl extends AnalyzeParam implements MethodInterceptor {
    private List<ParamInfo> returnParams;

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        stringBuilder.setLength(0);
        returnParams.clear();
        reflectParams(method.getGenericReturnType(), "", returnParams);
        try {
            return methodProxy.invokeSuper(object, args);
        } catch (Exception ignored) {
        }
        return null;
    }

    public List<ParamInfo> getReturnParams() {
        return returnParams;
    }

    public void setReturnParams(List<ParamInfo> returnParams) {
        this.returnParams = returnParams;
    }
}
