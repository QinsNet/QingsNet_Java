package com.ethereal.server.Utils;

import com.ethereal.server.Core.Annotation.BaseParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

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
