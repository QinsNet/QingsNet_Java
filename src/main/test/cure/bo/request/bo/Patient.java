package cure.bo.request.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Patient extends User{
    //诊断数据
    protected String listenResult;
    protected String lookResult;
    protected String feelPulseResult;
    protected String questionResult;
    //病人行为
    public abstract boolean listen();
    public abstract boolean look();
    public abstract boolean feel();
    public abstract boolean question();
}
