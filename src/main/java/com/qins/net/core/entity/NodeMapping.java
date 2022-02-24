package com.qins.net.core.entity;

import com.qins.net.meta.annotation.Sync;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeMapping {
    @Sync
    String mapping;
    @Sync
    String instance;
}
