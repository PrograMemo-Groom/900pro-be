package programo._pro.dto.userDto;

import programo._pro.entity.User;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;


@Data
@AllArgsConstructor
@Builder
public class UserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // 권장되는 직렬화 버전 UID
    private final Long id; // 👈 추가
    private final String email;
    private final String username;
    private String password;

    public void encryptPassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .username(username)
                .password(encodedPassword)
                .isActive(true)
                .build();
    }
}
