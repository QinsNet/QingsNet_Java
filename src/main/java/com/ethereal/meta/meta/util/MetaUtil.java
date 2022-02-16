package com.ethereal.meta.meta.util;

import com.ethereal.meta.meta.Meta;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class MetaUtil {
    public static Meta findMeta(Meta meta,String mapping){
        LinkedList<String> mappings = new LinkedList<>(Arrays.asList(mapping.split("/")));
        return findMeta(meta,mappings);
    }
    public static Meta findMeta(Meta meta,LinkedList<String> mappings){
        for(String name : mappings){
            if (meta.getMetas().containsKey(name)){
                meta = meta.getMetas().get(name);
            }
            else {
                return null;
            }
        }
        return meta;
    }
}
