package com.qins.net.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Http2Util {
    public static HashMap<String,String> getQuery(String raw_query) throws IllegalArgumentException {
        HashMap<String,String> query = new HashMap<>();
        if(raw_query == null)return query;
        for(String raw_pair : raw_query.split("&")){
            String[] pair = raw_pair.split("=");
            if(pair.length == 2){
                query.put(pair[0],pair[1]);
            }
            else throw new IllegalArgumentException(String.format("Query中的%s参数请求有误", raw_pair));
        }
        return query;
    }
    public static Map<String, String> getURLParamsFromChannel(FullHttpRequest fullHttpRequest) {
        Map<String, String> params = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
        Map<String, List<String>> paramList = decoder.parameters();
        for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
            params.put(entry.getKey(), entry.getValue().get(0));
        }
        return params;
    }

    public static Map<String, String> getBodyParamsChannel(FullHttpRequest fullHttpRequest) {
        Map<String, String> params;
        if (fullHttpRequest.method() == HttpMethod.POST) {
            String strContentType = fullHttpRequest.headers().get("Content-type").trim();
//            if (strContentType.contains("x-www-form-urlencoded")) {
            if (strContentType.contains("form")) {
                params = getFormParams(fullHttpRequest);
            }
            else if (strContentType.contains("application/json")) {
                try {
                    params = getJSONParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            } else {
                return null;
            }
            return params;
        }
        return null;
    }

    public static Map<String, String> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, String> params = new HashMap<>();
        // HttpPostMultipartRequestDecoder
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        return params;
    }

    public static Map<String, String> getJSONParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");
        Map<String ,String > params = SerializeUtil.gson.fromJson(strContent,HashMap.class);
        if(params == null)params = new HashMap<>();
        return params;
    }
}
