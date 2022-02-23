package com.qins.net.service.annotation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceMapping {
        ServiceType method;
        String mapping;
        int timeout;
}