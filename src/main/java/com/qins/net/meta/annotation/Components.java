package com.qins.net.meta.annotation;

import com.qins.net.meta.core.*;
import com.qins.net.meta.standard.*;
import com.qins.net.request.core.Request;
import com.qins.net.request.standard.StandardRequest;
import com.qins.net.service.core.Service;
import com.qins.net.service.standard.StandardService;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Components()
public @interface Components {
    Class<? extends BaseClass> primitiveClass() default PrimitiveBaseClass.class;
    Class<? extends BaseClass> referenceClass() default StandardBaseClass.class;
    Class<? extends MetaClass> metaClass() default StandardMetaClass.class;
    Class<? extends MetaField> metaField() default StandardMetaField.class;
    Class<? extends MetaMethod> metaMethod() default StandardMetaMethod.class;
    Class<? extends MetaParameter> metaParameter() default StandardMetaParameter.class;
    Class<? extends Request> request() default StandardRequest.class;
    Class<? extends Service> service() default StandardService.class;
}
