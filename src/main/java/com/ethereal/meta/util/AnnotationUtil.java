package com.ethereal.meta.util;

import javax.swing.text.Document;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.reflect.Method;

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
}
