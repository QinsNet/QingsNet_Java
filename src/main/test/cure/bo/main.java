package cure.bo;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.NewInstanceException;
import cure.bo.request.bo.Patient;

public class main {

public main(String[] args) throws NewInstanceException {
    Patient patient = MetaApplication.create(Patient.class);
    if(patient.login()){
        System.out.println(patient.getUsername() + "登录成功");
    }
    else System.out.println(patient.getUsername() + "登录失败");
    if(patient.feel()){
        System.out.println(patient.getFeelPulseResult());
    }
    else System.out.println("切诊失败");
    if(patient.listen()){
        System.out.println(patient.getListenResult());
    }
    else System.out.println("闻诊失败");
    if(patient.look()){
        System.out.println(patient.getLookResult());
    }
    else System.out.println("望诊失败");
    if(patient.question()){
        System.out.println(patient.getQuestionResult());
    }
    else System.out.println("问诊失败");
}
}
