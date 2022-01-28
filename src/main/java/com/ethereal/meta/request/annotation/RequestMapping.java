package com.ethereal.meta.request.annotation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMapping {
        RequestType method;
        String mapping;
        int invoke;
        int timeout;
}