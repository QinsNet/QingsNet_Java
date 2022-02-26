package com.qins.net.meta.annotation;

import com.qins.net.meta.cglib.CGLibClass;
import com.qins.net.meta.core.*;
import com.qins.net.meta.standard.StandardBaseClass;
import com.qins.net.meta.standard.StandardMetaClass;
import com.qins.net.meta.standard.StandardMetaField;
import com.qins.net.meta.standard.StandardMetaParameter;
import com.qins.net.request.cglib.CGLibRequest;
import com.qins.net.request.core.Request;
import com.qins.net.service.cglib.CGLibService;
import com.qins.net.service.core.Service;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Components()
public @interface Components {
    Class<? extends BaseClass> baseClass() default StandardBaseClass.class;
    Class<? extends MetaField> metaField() default StandardMetaField.class;
    Class<? extends MetaParameter> metaParameter() default StandardMetaParameter.class;
    Class<? extends MetaClass> metaClass() default CGLibClass.class;
    Class<? extends Request> request() default CGLibRequest.class;
    Class<? extends Service> service() default CGLibService.class;
}
