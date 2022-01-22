package com.ethereal.net.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class AnnotationUtils {
    public static Annotation getAnnotation(AnnotatedElement type, Class target){
        for (Annotation annotation : type.getAnnotations()){
            if(annotation.getClass() == target){
                return annotation;
            }
            return annotation.annotationType().getAnnotation(target);
        }
        return null;
    }
}
