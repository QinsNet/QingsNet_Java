package com.ethereal.net.core.base;

import com.ethereal.net.core.manager.type.AbstractTypeManager;
import com.ethereal.net.core.manager.ioc.IocManager;
import lombok.Getter;

public class MZCore extends BaseCore{
    @Getter
    protected AbstractTypeManager types = new AbstractTypeManager();
    @Getter
    protected IocManager iocManager = new IocManager();
}
