package com.qins.net.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.qins.net.meta.annotation.serialize.Sync;
import org.yaml.snakeyaml.Yaml;

public class SerializeUtil {
    public static Gson gson = new Gson()
            .newBuilder()
            .serializeNulls()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    return fieldAttributes.getAnnotation(Sync.class) == null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            })
            .create();
    public static Yaml yaml = new Yaml();

    public static String getStringOrNull(JsonElement value){
        if(value == null || value.isJsonNull())return null;
        return value.getAsString();
    }
}
