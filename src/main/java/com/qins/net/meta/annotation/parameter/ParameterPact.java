package com.qins.net.meta.annotation.parameter;

import com.qins.net.core.lang.serialize.SerializeLang;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ParameterPact {
    private String name;
    SerializeLang serializeLang;
    SerializeLang deserializeLang;
}
