package com.qins.net.util;

import com.qins.net.meta.annotation.MethodMapping;
import com.qins.net.node.annotation.GetMapping;
import com.qins.net.node.annotation.PostMapping;
import com.qins.net.request.annotation.MethodPact;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

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
    public static ArrayList<Field> getFields(Class<?> checkClass, Class<? extends Annotation> target){
        ArrayList<Field> fields = new ArrayList<>();
        while (checkClass != null){
            for (Field field : checkClass.getDeclaredFields()){
                if(field.getAnnotation(target) != null){
                    fields.add(field);
                }
            }
            checkClass = checkClass.getSuperclass();
        }
        return fields;
    }
    public static ArrayList<Field> getAllFields(Class<?> checkClass){
        ArrayList<Field> fields = new ArrayList<>();
        while (checkClass != null){
            fields.addAll(Arrays.asList(checkClass.getDeclaredFields()));
            checkClass = checkClass.getSuperclass();
        }
        return fields;
    }
    public static ArrayList<Method> getMethods(Class<?> checkClass, Class<? extends Annotation> target){
        ArrayList<Method> methods = new ArrayList<>();
        while (checkClass != null){
            for (Method method : checkClass.getDeclaredMethods())
                if (getAnnotation(method,target) != null) {
                    methods.add(method);
                }
            checkClass = checkClass.getSuperclass();
        }
        return methods;
    }
    public static MethodPact getMethodPact(Method method){
        if(method.getAnnotation(PostMapping.class) != null){
            MethodPact methodPact = new MethodPact();
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            methodPact.setTimeout(annotation.timeout());
            methodPact.setNodeClass(annotation.node());
            return methodPact;
        }
        else if(method.getAnnotation(GetMapping.class) != null){
            MethodPact methodPact = new MethodPact();
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            methodPact.setTimeout(annotation.timeout());
            methodPact.setNodeClass(annotation.node());
            return methodPact;
        }
        else if(method.getAnnotation(MethodMapping.class) != null){
            MethodPact methodPact = new MethodPact();
            MethodMapping annotation = method.getAnnotation(MethodMapping.class);
            methodPact.setTimeout(annotation.timeout());
            methodPact.setNodeClass(annotation.node());
            return methodPact;
        }
        else {
            MethodPact methodPact = new MethodPact();
            MethodMapping annotation = MethodMapping.class.getAnnotation(MethodMapping.class);
            methodPact.setTimeout(annotation.timeout());
            methodPact.setNodeClass(annotation.node());
            return methodPact;
        }
    }
}
