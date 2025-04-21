package programo._pro.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserInfoUpdateDto {

    @Size(max = 8, message = "닉네임은 최대 8자까지 가능합니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣]*$", // 한글 닉네임도 가능
            message = "닉네임은 한글, 영문자, 숫자만 사용할 수 있습니다."
    )
    @Schema(description = "사용자 닉네임", example = "엄준식")
    private String username;

    // 한글, 공백, 이모지 자동 차단
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*?])[A-Za-z\\d~!@#$%^&*?]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문자, 숫자, ~!@#$%^&*? 중 하나 이상의 특수문자를 포함해야 합니다."
    )
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    @Schema(description = "사용자 비밀번호", example = "test!123")
    private String password;
}
