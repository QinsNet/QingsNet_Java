package com.ethereal.meta.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;

public class SerializeUtil {
    public static Gson gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).excludeFieldsWithoutExposeAnnotation().serializeNulls().create();

}
