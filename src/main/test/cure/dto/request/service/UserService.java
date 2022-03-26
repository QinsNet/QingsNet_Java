package cure.dto.request.service;

import cure.dto.request.dto.LoginRequest;
import cure.dto.request.dto.LoginResponse;

public abstract class UserService {
    public abstract LoginResponse login(LoginRequest request);
}
