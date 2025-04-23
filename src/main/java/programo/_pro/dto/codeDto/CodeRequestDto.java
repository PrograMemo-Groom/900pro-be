package programo._pro.dto.codeDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CodeRequestDto {
    private int test_id;
    private int problem_id;
    private int user_id;
}
