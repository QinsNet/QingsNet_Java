package cure.dto.service.service;

import cure.dto.request.dto.LoginRequest;
import cure.dto.request.dto.LoginResponse;
import cure.dto.service.entity.User;

public class UserService {
    public LoginResponse login(LoginRequest request){
        LoginResponse response = new LoginResponse();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        if("839336369".equals(user.getUsername()) && "12345678".equals(user.getPassword())){
            user.setApiToken("abc123");
            response.setSuccess(true);
            response.setApiToken(user.getApiToken());
        }
        response.setSuccess(false);
        return response;
    }
}
