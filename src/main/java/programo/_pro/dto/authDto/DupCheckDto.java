package programo._pro.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class DupCheckDto {

    @Schema(description = "사용자 이메일", example = "test123@example.com")
    private String email;
}
