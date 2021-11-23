package com.ethereal.server.Core.BaseCore;

import com.ethereal.server.Core.Manager.AbstractType.AbstractTypeManager;
import com.ethereal.server.Core.Manager.Event.EventManager;
import com.ethereal.server.Core.Manager.Ioc.IocManager;

public class MZCore extends BaseCore{
    protected AbstractTypeManager types = new AbstractTypeManager();
    protected IocManager iocManager = new IocManager();

    public IocManager getIocManager() {
        return iocManager;
    }

    public void setIocManager(IocManager iocManager) {
        this.iocManager = iocManager;
    }

    public AbstractTypeManager getTypes() {
        return types;
    }

    public void setTypes(AbstractTypeManager types) {
        this.types = types;
    }
}
