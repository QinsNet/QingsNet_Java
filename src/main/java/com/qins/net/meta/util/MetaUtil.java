package com.qins.net.meta.util;

import com.qins.net.meta.core.MetaNodeField;

import java.util.Arrays;
import java.util.LinkedList;

public class MetaUtil {
    public static MetaNodeField findMeta(MetaNodeField metaNodeField, String mapping){
        LinkedList<String> mappings = new LinkedList<>(Arrays.asList(mapping.split("/")));
        return findMeta(metaNodeField,mappings);
    }
    public static MetaNodeField findMeta(MetaNodeField metaNodeField, LinkedList<String> mappings){
        for(String name : mappings){
            if (metaNodeField.getMetas().containsKey(name)){
                metaNodeField = metaNodeField.getMetas().get(name);
            }
            else {
                return null;
            }
        }
        return metaNodeField;
    }
}
