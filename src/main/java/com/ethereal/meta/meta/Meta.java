package com.ethereal.meta.meta;

import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.node.core.Node;
import lombok.Getter;

import java.util.HashMap;

public abstract class Meta extends Node {
    @Getter
    private HashMap<String,Meta> metas = new HashMap<>();
    public Meta() throws TrackException {

    }
    public static
}
