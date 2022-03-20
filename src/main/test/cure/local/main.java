package cure.local;

import cure.local.bo.FeelPulse;
import cure.local.bo.Question;
import cure.local.service.FeelPulseService;
import cure.local.service.QuestionService;

public class main {

    public static void main(String[] args) {
        FeelPulse feelPulse = new FeelPulseService();
        if(feelPulse.feel()){
            System.out.println(feelPulse.getResult());
        }
        else System.out.println("切诊失败");
    }
}
