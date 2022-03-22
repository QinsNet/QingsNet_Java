package com.qins.net.util;

import com.qins.net.meta.annotation.field.Async;
import com.qins.net.meta.annotation.field.FieldPact;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.meta.annotation.instance.MetaPact;
import com.qins.net.meta.annotation.method.MethodPact;
import com.qins.net.meta.annotation.parameter.ParameterPact;
import com.qins.net.node.annotation.Get;
import com.qins.net.node.annotation.Post;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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
    public static ArrayList<Field> getMetaFields(Class<?> checkClass){
        ArrayList<Field> fields = new ArrayList<>();
        while (checkClass != null){
            for (Field field : checkClass.getDeclaredFields()){
                if(getFieldPact(field) != null){
                    fields.add(field);
                }
            }
            checkClass = checkClass.getSuperclass();
        }
        return fields;
    }
    public static ArrayList<Method> getMetaMethods(Class<?> checkClass){
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
    public static MethodPact getMethodPact(Method method){
        if(method.getAnnotation(Post.class) != null){
            MethodPact pact = new MethodPact();
            Post annotation = method.getAnnotation(Post.class);
            pact.setName("".equals(annotation.value())? method.getName() : annotation.value())
                    .setTimeout(annotation.timeout())
                    .setNodes(new HashSet<>(Arrays.asList(annotation.nodes())))
                    .setNodeClass(annotation.node());
            return pact;
        }
        else if(method.getAnnotation(Get.class) != null){
            MethodPact pact = new MethodPact();
            Get annotation = method.getAnnotation(Get.class);
            pact.setName("".equals(annotation.value())? method.getName() : annotation.value())
                    .setTimeout(annotation.timeout())
                    .setNodes(new HashSet<>(Arrays.asList(annotation.nodes())))
                    .setNodeClass(annotation.node());
            return pact;
        }
        return null;
    }
    public static FieldPact getFieldPact(Field field){
        if(field.getAnnotation(Sync.class) != null){
            FieldPact pact = new FieldPact();
            Sync annotation = field.getAnnotation(Sync.class);
            pact.setName("".equals(annotation.value())? field.getName() : annotation.value())
                    .setSync(true);
            return pact;
        }
        else if(field.getAnnotation(Async.class) != null){
            FieldPact pact = new FieldPact();
            Async annotation = field.getAnnotation(Async.class);
            pact.setName("".equals(annotation.value())? field.getName() : annotation.value())
                    .setNodes(annotation.nodes())
                    .setSync(false);
            return pact;
        }
        return null;
    }

    public static MetaPact getMetaPact(Class<?> klass){
        if(klass.getAnnotation(Meta.class) != null){
            MetaPact pact = new MetaPact();
            Meta annotation = klass.getAnnotation(Meta.class);
            pact.setName("".equals(annotation.value())? klass.getSimpleName() : annotation.value())
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

    public static ParameterPact getParameterPact(Parameter parameter){
        if(parameter.getAnnotation(Sync.class) != null){
            ParameterPact pact = new ParameterPact();
            Sync annotation = parameter.getAnnotation(Sync.class);
            pact.setName("".equals(annotation.value())? parameter.getName() : annotation.value());
            pact.setSync(true);
            return pact;
        }
        if(parameter.getAnnotation(Async.class) != null){
            ParameterPact pact = new ParameterPact();
            Async annotation = parameter.getAnnotation(Async.class);
            pact.setName("".equals(annotation.value())? parameter.getName() : annotation.value());
            pact.setSync(false);
            return pact;
        }
        return null;
    }
}
