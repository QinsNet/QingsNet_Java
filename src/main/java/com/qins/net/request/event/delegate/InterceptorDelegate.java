package com.qins.net.request.event.delegate;

import com.qins.net.core.entity.RequestMeta;
import com.qins.net.request.core.Request;

public interface InterceptorDelegate {
    boolean onInterceptor(Request request, RequestMeta requestMeta);
}
