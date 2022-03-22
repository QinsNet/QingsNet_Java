package com.qins.net.core.aop.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class EventContext {
    private Map<String, Object> parameters;
    private Method method;
}
