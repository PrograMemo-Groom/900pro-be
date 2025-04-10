package programo._pro.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SignUpDto {
    private String email;
    private String username;
    private String password;

    public UserInfo toService() {
        return UserInfo.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
    }
}
