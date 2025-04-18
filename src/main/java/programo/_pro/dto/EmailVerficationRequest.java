package programo._pro.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class EmailVerficationRequest {
    @Email
    private String email;

    private int code;
}
