package programo._pro.dto.teamDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programo._pro.entity.Level;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TeamCreateRequest {

    @Schema(description = "팀 이름", example = "코테 참 잘하는 팀")
    @NotBlank
    private String teamName;

    @Schema(description = "팀 설명", example = "안녕하세요 “프로그램(Program)”과 “Memo(기억, 기록)”를 합쳐, 함께 개발하며 기억에 남는 성과를 만들어가는  팀 “프로그래모(PrograMemo)” 입니다")
    private String description;

    @Schema(description = "팀 난이도", example = "하", implementation = Level.class)
    @NotNull
    private Level level;

    @Schema(description = "문제 개수(3~5)", example = "3")
    @Min(3)
    @Max(5)
    private int problemCount;

    @Schema(description = "시작 시간시작 시간", example = "2025-04-22T10:00:00")
    @NotNull
    private LocalDateTime startTime;

    @Schema(description = "응시 시간 (모의 코테 응시 시각)\n" + "(2,3,4,5시간)", example = "3")
    @Min(2)
    @Max(5)
    private int durationTime;
}
