package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.request.annotation.MethodPact;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

@Getter
@Setter
public abstract class MetaMethod {
    private Method method;
    private String name;
    private Set<String> nodes;
    private HashMap<String, MetaParameter> parameters;
    private HashMap<String, MetaParameter> metaParameters;
    private BaseClass metaReturn;
    private MethodPact methodPact;
    public MetaMethod(Method method,Components components) throws LoadClassException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, TrackException {
        this.method = method;
        this.methodPact = AnnotationUtil.getMethodPact(method);
        Meta meta = method.getAnnotation(Meta.class);
        this.name = "".equals(meta.value()) ? method.getName() : meta.value();
        if(meta.nodes().length != 0){
            nodes = new HashSet<>(Arrays.asList(meta.nodes()));
        }
        else nodes = new HashSet<>();
        if(method.getReturnType() != void.class && method.getReturnType() != Void.class){
            Type returnType = method.getGenericReturnType();
            metaReturn = MetaApplication.getContext().getMetaClassLoader().loadClass(returnType.getTypeName(),method.getReturnType());
        }
        parameters = new HashMap<>();
        metaParameters = new HashMap<>();
        for (Parameter parameter : method.getParameters()){
            MetaParameter metaParameter = components.metaParameter()
                    .getConstructor(Parameter.class,Components.class)
                    .newInstance(parameter,components);
            parameters.put(metaParameter.getName(),metaParameter);
            Meta paramMeta = parameter.getAnnotation(Meta.class);
            if(paramMeta != null){
                metaParameters.put(metaParameter.getName(),metaParameter);
            }
        }
    }
}
