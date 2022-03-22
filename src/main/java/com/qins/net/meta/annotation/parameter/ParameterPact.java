package com.qins.net.meta.annotation.parameter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ParameterPact {
    String name;
    boolean sync = true;
}
