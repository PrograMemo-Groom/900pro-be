package programo._pro.dto.authDto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SignInDto {

    @Schema(description = "사용자 이메일", example = "test123@example.com")
    private String email;

    @Schema(description = "사용자 비밀번호", example = "test123!@")
    private String password;
}
