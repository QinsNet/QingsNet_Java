package Service.Abstract;

import Core.Model.AbstractTypes;

public class ServiceConfig {
    private AbstractTypes types;

    public AbstractTypes getTypes() {
        return types;
    }

    public void setTypes(AbstractTypes types) {
        this.types = types;
    }

    public ServiceConfig(AbstractTypes types) {
        this.types = types;
    }
}
