package com.qins.net.util;

import com.qins.net.core.exception.ObjectLangException;
import com.qins.net.core.lang.serialize.ObjectLang;
import com.qins.net.meta.annotation.field.FieldPact;
import com.qins.net.meta.annotation.field.Field;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.meta.annotation.instance.MetaPact;
import com.qins.net.meta.annotation.method.MethodPact;
import com.qins.net.meta.annotation.parameter.MetaParam;
import com.qins.net.meta.annotation.parameter.ParameterPact;
import com.qins.net.meta.annotation.serialize.ReturnAsync;
import com.qins.net.meta.annotation.returnval.ReturnPact;
import com.qins.net.meta.annotation.serialize.ReturnSync;
import com.qins.net.meta.annotation.serialize.*;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.node.annotation.Post;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public class AnnotationUtil {
    public static Annotation getAnnotation(Method method, Class<? extends Annotation> target){
        for (Annotation annotation : method.getAnnotations()){
            annotation = getAnnotation(annotation.annotationType(),target);
            if(annotation != null){
                return annotation;
            }
        }
        return null;
    }

    public static Annotation getAnnotation(Class<?> root, Class<? extends Annotation> target){
        for (Annotation annotation : root.getDeclaredAnnotations()){
            if(annotation.annotationType() == target){
                return annotation;
            }
            else {
                annotation = getAnnotation(annotation.annotationType(),target);
                if(annotation != null){
                    return annotation;
                }
            }
        }
        return null;
    }


    public static ArrayList<java.lang.reflect.Field> getMetaFields(Class<?> checkClass){
        ArrayList<java.lang.reflect.Field> fields = new ArrayList<>();
        while (checkClass != null){
            for (java.lang.reflect.Field field : checkClass.getDeclaredFields()){
                if(getFieldPact(field) != null){
                    fields.add(field);
                }
            }
            checkClass = checkClass.getSuperclass();
        }
        return fields;
    }

    public static ArrayList<Method> getMetaMethods(Class<?> checkClass) throws ObjectLangException {
        ArrayList<Method> methods = new ArrayList<>();
        while (checkClass != null){
            for (Method method : checkClass.getDeclaredMethods())
                if (getMethodPact(method) != null) {
                    methods.add(method);
                }
            checkClass = checkClass.getSuperclass();
        }
        return methods;
    }

    public static MetaPact getMetaPact(Class<?> klass){
        if(klass.getAnnotation(Meta.class) != null){
            Meta annotation = klass.getAnnotation(Meta.class);
            MetaPact pact = new MetaPact()
                    .setName("".equals(annotation.value())? klass.getSimpleName() : annotation.value())
                    .setNodes(new HashSet<>(Arrays.asList(annotation.nodes())));
            klass = klass.getSuperclass();
            while (klass != null){
                MetaPact superPact = getMetaPact(klass);
                if(superPact == null)break;
                pact.getNodes().addAll(superPact.getNodes());
                klass = klass.getSuperclass();
            }
            return pact;
        }
        return null;
    }

    public static FieldPact getFieldPact(java.lang.reflect.Field field){
        if(field.getAnnotation(Field.class) != null){
            Field annotation = field.getAnnotation(Field.class);
            return new FieldPact().setName("".equals(annotation.value())? field.getName() : annotation.value());
        }
        return null;
    }

    public static MethodPact getMethodPact(Method method) throws ObjectLangException {
        if(method.getAnnotation(Post.class) != null){
            Post annotation = method.getAnnotation(Post.class);
            MethodPact pact = new MethodPact()
                    .setName("".equals(annotation.value())? method.getName() : annotation.value())
                    .setTimeout(annotation.timeout())
                    .setNodes(new HashSet<>(Arrays.asList(annotation.nodes())))
                    .setNodeClass(annotation.node())
                    .setSerializeLang(new SerializeLang())
                    .setDeserializeLang(new SerializeLang());
            if(method.getAnnotation(SendSync.class) != null){
                pact.getSerializeLang().setSync(ObjectLang.process(method.getAnnotation(SendSync.class).value()));
            }
            if(method.getAnnotation(SendAsync.class) != null){
                pact.getSerializeLang().setAsync(ObjectLang.process(method.getAnnotation(SendAsync.class).value()));
            }
            if(method.getAnnotation(ReceiveSync.class) != null){
                pact.getDeserializeLang().setSync(ObjectLang.process(method.getAnnotation(ReceiveSync.class).value()));
            }
            if(method.getAnnotation(ReceiveAsync.class) != null){
                pact.getDeserializeLang().setAsync(ObjectLang.process(method.getAnnotation(ReceiveAsync.class).value()));
            }
            return pact;
        }
        return null;
    }

    public static ParameterPact getParameterPact(Parameter parameter) throws ObjectLangException {
        ParameterPact pact = new ParameterPact()
                .setName(Optional.ofNullable(parameter.getAnnotation(MetaParam.class)).map(MetaParam::value).orElse(parameter.getName()))
                .setSerializeLang(new SerializeLang())
                .setDeserializeLang(new SerializeLang());
        if(parameter.getAnnotation(SendSync.class) != null){
            pact.getSerializeLang().setSync(ObjectLang.process(parameter.getAnnotation(SendSync.class).value()));
        }
        if(parameter.getAnnotation(SendAsync.class) != null){
            pact.getSerializeLang().setAsync(ObjectLang.process(parameter.getAnnotation(SendAsync.class).value()));
        }
        if(parameter.getAnnotation(ReceiveSync.class) != null){
            pact.getDeserializeLang().setSync(ObjectLang.process(parameter.getAnnotation(ReceiveSync.class).value()));
        }
        if(parameter.getAnnotation(ReceiveAsync.class) != null){
            pact.getDeserializeLang().setAsync(ObjectLang.process(parameter.getAnnotation(ReceiveAsync.class).value()));
        }
        return pact;
    }

    public static ReturnPact getMethodReturnPact(Method method) throws ObjectLangException {
        ReturnPact pact = new ReturnPact()
                .setMutualSerialize(new SerializeLang());
        if(method.getAnnotation(ReturnSync.class) != null){
            pact.getMutualSerialize().setSync(ObjectLang.process(method.getAnnotation(ReturnSync.class).value()));
        }
        if(method.getAnnotation(ReturnAsync.class) != null){
            pact.getMutualSerialize().setAsync(ObjectLang.process(method.getAnnotation(ReturnAsync.class).value()));
        }
        return pact;
    }

}
