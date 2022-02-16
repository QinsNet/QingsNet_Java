package com.ethereal.meta.meta.annotation;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.service.core.Service;
import com.ethereal.meta.standard.StandardMeta;
import com.ethereal.meta.standard.StandardRequest;
import com.ethereal.meta.standard.StandardService;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Components()
public @interface Components {
    Class<? extends Meta> meta() default StandardMeta.class;
    Class<? extends Request> request() default StandardRequest.class;
    Class<? extends Service> service() default StandardService.class;
}
