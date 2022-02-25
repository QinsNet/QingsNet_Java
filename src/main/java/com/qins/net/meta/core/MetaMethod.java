package com.qins.net.meta.core;

import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.request.annotation.MethodPact;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

@Getter
@Setter
public class MetaMethod {
    private Method method;
    private String name;
    private HashMap<String, MetaParameter> metaParameters;
    private HashMap<String, MetaParameter> metaSyncParameters;
    private BaseClass metaReturn;
    private MethodPact methodPact;
    public MetaMethod(Method method) throws LoadClassException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.method = method;
        this.methodPact = AnnotationUtil.getMethodPact(method);
        Meta meta = method.getAnnotation(Meta.class);
        this.name = "".equals(meta.value()) ? method.getName() : meta.value();
        MetaClassLoader metaClassLoader = (MetaClassLoader) Thread.currentThread().getContextClassLoader();
        if(method.getReturnType() != void.class && method.getReturnType() != Void.class){
            metaReturn = metaClassLoader.loadClass(method.getReturnType()).getComponents().metaClass().getConstructor(Class.class).newInstance(method.getReturnType());
        }
        metaParameters = new HashMap<>();
        metaSyncParameters = new HashMap<>();
        for (Parameter parameter : method.getParameters()){
            MetaParameter metaParameter = metaClassLoader.loadClass(parameter.getType()).getComponents().metaParameter()
                    .getConstructor(Parameter.class)
                    .newInstance(parameter);
            metaParameters.put(name,metaParameter);
            Meta paramMeta = method.getAnnotation(Meta.class);
            if(paramMeta != null){
                String paramName = "".equals(paramMeta.value()) ? method.getName() : paramMeta.value();
                metaSyncParameters.put(paramName,metaParameter);
            }
        }
    }
}
