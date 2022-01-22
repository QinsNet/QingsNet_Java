package com.ethereal.net.core.entity;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;

@ToString
@Getter
@Setter
public class RequestMeta {
    @Expose(serialize = false,deserialize = false)
    private ResponseMeta result;
    @Expose
    private String type = "ER-1.0-Request";
    @Expose
    private String mapping;
    @Expose
    private HashMap<String ,String > params;
    @Expose
    private String id;
}
