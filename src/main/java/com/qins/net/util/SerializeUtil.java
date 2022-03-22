package com.qins.net.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.qins.net.meta.annotation.field.Sync;
import org.yaml.snakeyaml.Yaml;

public class SerializeUtil {
    public static Gson gson = new Gson()
            .newBuilder()
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
}
