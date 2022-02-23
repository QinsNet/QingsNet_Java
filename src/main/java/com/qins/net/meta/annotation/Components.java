package com.qins.net.meta.annotation;

import com.qins.net.meta.Meta;
import com.qins.net.request.core.Request;
import com.qins.net.service.core.Service;
import com.qins.net.standard.StandardMeta;
import com.qins.net.standard.StandardRequest;
import com.qins.net.standard.StandardService;

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
