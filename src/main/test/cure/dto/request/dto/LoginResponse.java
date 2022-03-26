package cure.dto.request.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    boolean success;
    String apiToken;
}
