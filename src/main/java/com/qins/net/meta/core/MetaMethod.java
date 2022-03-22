package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.annotation.method.MethodPact;
import com.qins.net.node.core.Node;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Getter
@Setter
public abstract class MetaMethod {
    private Method method;
    private String name;
    private Set<String> nodes;
    private HashMap<String, MetaParameter> parameters = new HashMap<>();
    private HashMap<String, MetaParameter> syncParameters = new HashMap<>();
    private BaseClass metaReturn;
    private int timeout;
    private Class<? extends Node> nodeClass;

    public MetaMethod(Method method,Components components) throws LoadClassException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, TrackException {
        this.method = method;
        MethodPact pact = AnnotationUtil.getMethodPact(method);
        assert pact != null;
        this.name = pact.getName();
        this.nodeClass = pact.getNodeClass();
        if(pact.getNodes() != null){
            nodes = pact.getNodes();
        }
        else nodes = new HashSet<>();
        metaReturn = MetaApplication.getContext().getMetaClassLoader().loadClass(method.getReturnType());
        for (Parameter parameter : method.getParameters()){
            MetaParameter metaParameter = components.metaParameter()
                    .getConstructor(Parameter.class,Components.class)
                    .newInstance(parameter,components);
            parameters.put(metaParameter.name,metaParameter);
            if(metaParameter.sync){
                syncParameters.put(metaParameter.name, metaParameter);
            }
        }
    }
}
