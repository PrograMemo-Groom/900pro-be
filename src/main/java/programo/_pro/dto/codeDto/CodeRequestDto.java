package programo._pro.dto.codeDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CodeRequestDto {
    private int testId;
    private int problemId;
    private int userId;
}
