package com.qins.net.core.entity;

import com.qins.net.meta.annotation.Meta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
public class QinsMeta {
    @Meta
    String instance;
    @Meta
    HashMap<String,String> nodes;
}
