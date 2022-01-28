package com.ethereal.meta.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {
    public static Annotation getAnnotation(Method method, Class<? extends Annotation> target){
        for (Annotation annotation : method.getAnnotations()){
            annotation = getAnnotation(annotation,target);
            if(annotation != null){
                return annotation;
            }
        }
        return null;
    }
    public static Annotation getAnnotation(Annotation root, Class<? extends Annotation> target){
        for (Annotation annotation : root.annotationType().getAnnotations()){
            if(annotation.annotationType() == target){
                return annotation;
            }
            else {
                annotation = getAnnotation(annotation,target);
                if(annotation != null){
                    return annotation;
                }
            }
        }
        return null;
    }
}
