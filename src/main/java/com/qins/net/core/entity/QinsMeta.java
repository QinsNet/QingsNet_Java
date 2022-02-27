package com.qins.net.core.entity;

import com.qins.net.meta.annotation.Meta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@Accessors(chain = true)
public class QinsMeta {
    @Meta
    Object instance;
    @Meta
    HashMap<String,String> nodes;
    public QinsMeta(){

    }
}
