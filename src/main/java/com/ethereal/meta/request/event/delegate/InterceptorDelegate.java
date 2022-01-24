package com.ethereal.meta.request.event.delegate;

import com.ethereal.meta.request.core.Request;

public interface InterceptorDelegate {
    boolean onInterceptor(Request request, com.ethereal.meta.core.entity.RequestMeta requestMeta);
}
