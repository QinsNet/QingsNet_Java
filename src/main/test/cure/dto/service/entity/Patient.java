package cure.dto.service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Patient extends User{
    //诊断数据
    protected String listenResult;
    protected String lookResult;
    protected String feelPulseResult;
    protected String questionResult;
}
