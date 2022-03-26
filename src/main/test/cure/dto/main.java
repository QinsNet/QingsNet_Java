package cure.dto;


import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.NewInstanceException;
import cure.dto.request.dto.*;
import cure.dto.request.service.PatientService;
import cure.dto.service.entity.Patient;

public class main {

    public static void main(String[] args) throws NewInstanceException {
        PatientService service = MetaApplication.create(PatientService.class);
        Patient patient = new Patient();
        patient.setUsername("839336369");
        patient.setPassword("123456");
        //登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(patient.getUsername());
        loginRequest.setPassword(patient.getPassword());
        LoginResponse loginResponse = service.login(loginRequest);
        if(loginResponse.isSuccess()){
            patient.setApiToken(loginResponse.getApiToken());
            System.out.println(patient.getUsername() + "登录成功");
        }
        else System.out.println(patient.getUsername() + "登录失败");

        FeelPulseRequest feelPulseRequest = new FeelPulseRequest();
        feelPulseRequest.setUsername(patient.getUsername());
        feelPulseRequest.setApiToken(patient.getFeelPulseResult());
        FeelPulseResponse feelPulseResponse = service.feel(feelPulseRequest);
        if(feelPulseResponse.isSuccess()){
            patient.setFeelPulseResult(feelPulseResponse.getResult());
            System.out.println(patient.getFeelPulseResult());
        }
        else System.out.println("切诊失败");

        LookRequest lookRequest = new LookRequest();
        lookRequest.setUsername(patient.getUsername());
        lookRequest.setApiToken(patient.getApiToken());
        LookResponse lookResponse = service.look(lookRequest);
        if(lookResponse.isSuccess()){
            patient.setFeelPulseResult(lookResponse.getResult());
            System.out.println(patient.getLookResult());
        }
        else System.out.println("切诊失败");

        QuestionRequest questionRequest = new QuestionRequest();
        questionRequest.setUsername(patient.getUsername());
        questionRequest.setApiToken(patient.getApiToken());
        QuestionResponse questionResponse = service.question(questionRequest);
        if(questionResponse.isSuccess()){
            patient.setFeelPulseResult(questionResponse.getResult());
            System.out.println(patient.getQuestionResult());
        }
        else System.out.println("切诊失败");

        ListenRequest listenRequest = new ListenRequest();
        listenRequest.setUsername(patient.getUsername());
        listenRequest.setApiToken(patient.getApiToken());
        ListenResponse listenResponse = service.listen(listenRequest);
        if(listenResponse.isSuccess()){
            patient.setFeelPulseResult(listenResponse.getResult());
            System.out.println(patient.getListenResult());
        }
        else System.out.println("切诊失败");
    }
}
