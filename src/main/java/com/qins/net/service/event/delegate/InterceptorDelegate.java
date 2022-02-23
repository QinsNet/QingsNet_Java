package com.qins.net.service.event.delegate;

import com.qins.net.core.entity.RequestMeta;

public interface InterceptorDelegate {
    boolean onInterceptor(RequestMeta requestMeta);
}
