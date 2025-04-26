package programo._pro.dto.teamDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamCardDto {

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "코테 최강 프로그래모")
    private String teamName;

    @Schema(description = "팀 설명", example = "우리팀 짱짱임 들어오셈")
    private String description;

    @Schema(description = "난이도", example = "HARD")
    private String level;

    @Schema(description = "시작 시간 (HH:mm 포맷)", example = "14:00")
    private String startTime;

    @Schema(description = "문제 개수", example = "3")
    private int problemCount;

    @Schema(description = "현재 팀원 수", example = "4")
    private int currentMembers;
}
