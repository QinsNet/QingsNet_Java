package com.qins.net.meta.annotation.returnval;

import com.qins.net.core.lang.serialize.SerializeLang;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ReturnPact {
    private SerializeLang serializeLang;
}
