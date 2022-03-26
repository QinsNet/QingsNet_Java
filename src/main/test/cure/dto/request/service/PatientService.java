package cure.dto.request.service;

import cure.dto.request.dto.*;
import cure.dto.service.service.UserService;

public abstract class PatientService extends UserService {
    public abstract FeelPulseResponse feel(FeelPulseRequest request);
    public abstract QuestionResponse question(QuestionRequest request);
    public abstract ListenResponse listen(ListenRequest request);
    public abstract LookResponse look(LookRequest request);
}
