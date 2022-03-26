package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.method.MethodPact;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.node.core.Node;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

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
    private MetaReturn metaReturn;
    private int timeout;
    private Class<? extends Node> nodeClass;
    private SerializeLang instanceSerializeLang;

    public MetaMethod(Method method,Components components) throws NewInstanceException {
        try {
            this.method = method;
            MethodPact pact = AnnotationUtil.getMethodPact(method);
            assert pact != null;
            this.name = pact.getName();
            this.nodeClass = pact.getNodeClass();
            this.instanceSerializeLang = pact.getSerializeLang();

            if(pact.getNodes() != null){
                nodes = pact.getNodes();
            }
            else nodes = new HashSet<>();
            metaReturn = components.metaReturn()
                    .getConstructor(Method.class,Components.class)
                    .newInstance(method,components);
            for (Parameter parameter : method.getParameters()){
                MetaParameter metaParameter = components.metaParameter()
                        .getConstructor(Parameter.class,Components.class)
                        .newInstance(parameter,components);
                parameters.put(metaParameter.name,metaParameter);
            }
        }
        catch (Exception e){
            throw new NewInstanceException(e);
        }
    }
}
