package programo._pro.dto.problemDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProblemGenerateRequestDto {
    private long teamId;
    private int problemCount;
    private LocalDateTime startTime;
}
