package com.ethereal.meta.service.event.delegate;

import com.ethereal.meta.core.entity.RequestMeta;

public interface InterceptorDelegate {
    boolean onInterceptor(RequestMeta requestMeta);
}
