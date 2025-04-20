package programo._pro.dto.authDto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import programo._pro.dto.userDto.UserInfo;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class) // camelCase -> snake_case 자동변환
public class SignUpDto {

    @Email
    private String email;

    @Size(max = 8, message = "닉네임은 최대 8자까지 가능합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "닉네임은 영문자와 숫자만 사용할 수 있습니다.")
    private String username;

    // 한글, 공백, 이모지 자동 차단
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*?])[A-Za-z\\d~!@#$%^&*?]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문자, 숫자, ~!@#$%^&*? 중 하나 이상의 특수문자를 포함해야 합니다."
    )
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    private String password;

    // 내부 처리용 DTO
    public UserInfo toService() {
        return UserInfo.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
    }
}
