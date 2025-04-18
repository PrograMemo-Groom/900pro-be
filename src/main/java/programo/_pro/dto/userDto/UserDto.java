package programo._pro.dto.userDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
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

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
