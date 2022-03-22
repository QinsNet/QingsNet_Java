package com.qins.net.meta.annotation.field;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FieldPact {
    String name;
    String[] nodes;
    boolean sync = true;
}
