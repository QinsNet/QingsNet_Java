package com.qins.net.meta.util;

import com.qins.net.meta.Meta;

import java.util.Arrays;
import java.util.LinkedList;

public class MetaUtil {
    public static Meta findMeta(Meta meta, String mapping){
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
