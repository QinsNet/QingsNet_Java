package com.ethereal.net.request.event.delegate;

import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.service.core.Service;

public interface InterceptorDelegate {
    boolean onInterceptor(Request request, RequestMeta requestMeta);
}
