package com.qins.net.meta.annotation.instance;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class MetaPact {
    String name;
    Set<String> nodes;
}
