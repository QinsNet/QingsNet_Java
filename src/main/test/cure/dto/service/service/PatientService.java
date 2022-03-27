package cure.dto.service.service;

import cure.dto.request.dto.*;
public class PatientService extends UserService  {
    public FeelPulseResponse feel(FeelPulseRequest request){
        FeelPulseResponse response = new FeelPulseResponse();
        if("123abc".equals(request.getApiToken())){
            response.setResult(request.getUsername() + "切诊结果");
            response.setSuccess(true);
        }
        response.setSuccess(false);
        return response;
    }
    public QuestionResponse question(QuestionRequest request){
        QuestionResponse response = new QuestionResponse();
        if("123abc".equals(request.getApiToken())){
            response.setResult(request.getUsername() + "问诊结果");
            response.setSuccess(true);
        }
        response.setSuccess(false);
        return response;
    }
    public ListenResponse listen(ListenRequest request){
        ListenResponse response = new ListenResponse();
        if("123abc".equals(request.getApiToken())){
            response.setResult(request.getUsername() + "闻诊结果");
            response.setSuccess(true);
        }
        response.setSuccess(false);
        return response;
    }
    public LookResponse look(LookRequest request){
        LookResponse response = new LookResponse();
        if("123abc".equals(request.getApiToken())){
            response.setResult(request.getUsername() + "望诊结果");
            response.setSuccess(true);
        }
        response.setSuccess(false);
        return response;
    }

}
