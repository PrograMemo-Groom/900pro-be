package programo._pro.dto.codeDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateCodeDto {
    CodeRequestDto codeRequestDto;

    String language;
    String submitCode;
    LocalDateTime submitAt = LocalDateTime.now();
}
