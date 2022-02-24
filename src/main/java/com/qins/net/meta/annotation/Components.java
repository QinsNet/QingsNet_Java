package com.qins.net.meta.annotation;

import com.qins.net.component.StandardMetaNodeField;
import com.qins.net.component.StandardMetaNodeParameter;
import com.qins.net.component.StandardMetaNodeReturn;
import com.qins.net.meta.core.*;
import com.qins.net.meta.core.MetaNodeField;
import com.qins.net.request.core.Request;
import com.qins.net.service.core.Service;
import com.qins.net.component.*;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Components()
public @interface Components {
    Class<? extends MetaNodeField> metaNode() default StandardMetaNodeField.class;
    Class<? extends MetaNodeParameter> metaNodeParameter() default StandardMetaNodeParameter.class;
    Class<? extends MetaNodeReturn> metaNodeReturn() default StandardMetaNodeReturn.class;
    Class<? extends MetaReturn> metaReturn() default StandardMetaReturn.class;
    Class<? extends MetaField> metaField() default StandardMetaField.class;
    Class<? extends MetaParameter> metaParameter() default StandardMetaParameter.class;
    Class<? extends Request> request() default StandardRequest.class;
    Class<? extends Service> service() default StandardService.class;
}
