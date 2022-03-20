package cure.local.bo;

import lombok.Getter;

@Getter
public abstract class FeelPulse {
    String result;
    public abstract boolean feel();
}
