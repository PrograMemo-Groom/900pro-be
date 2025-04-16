package programo._pro.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_name", nullable = false)
    private String username;

    // 한글, 공백, 이모지 자동 차단
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*?])[A-Za-z\\d~!@#$%^&*?]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문자, 숫자, ~!@#$%^&*? 중 하나 이상의 특수문자를 포함해야 합니다."
    )
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    @Column(name = "password")
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

//    public boolean isActive() {
//        return flag == 1;
//    }
}
