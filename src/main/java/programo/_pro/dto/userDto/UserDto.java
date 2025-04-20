package programo._pro.dto.userDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class UserDto {

    private Long id;

    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Size(max = 8, message = "닉네임은 최대 8자까지 가능합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "닉네임은 영문자와 숫자만 사용할 수 있습니다.")
    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
