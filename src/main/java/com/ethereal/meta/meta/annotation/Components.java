package com.ethereal.meta.meta.annotation;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.service.core.Service;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Components()
public @interface Components {
    Class<? extends Meta> meta() default Meta.class;
    Class<? extends Request> request() default Request.class;
    Class<? extends Service> service() default Service.class;
    Class<? extends Net> net() default Net.class;
}
