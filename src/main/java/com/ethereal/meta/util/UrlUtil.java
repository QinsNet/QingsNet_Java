package com.ethereal.meta.util;

import java.util.HashMap;

public class UrlUtil {
    public static HashMap<String,String> getQuery(String raw_query) throws IllegalArgumentException {
        HashMap<String,String> query = new HashMap<>();
        for(String raw_pair : raw_query.split("&")){
            String[] pair = raw_pair.split("=");
            if(pair.length == 2){
                query.put(pair[0],pair[1]);
            }
            else throw new IllegalArgumentException(String.format("Query中的%s参数请求有误", raw_pair));
        }
        return query;
    }
}
