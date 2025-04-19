package programo._pro.dto.teamDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import programo._pro.entity.Team;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class TeamMainDto {

    @Schema(description = "팀 ID", example = "1")
    private Long id;

    @Schema(description = "팀 이름", example = "멧도리도리팀")
    private String teamName;

    @Schema(description = "팀 설명", example = "팀원 모두가 코딩테스트에 완벽하게 참여한 날에 멧돌이의 사진을 업데이트합니다.\n" +
            "귀여운 햄스터를 보고싶다면 지금 당장 공부하세요. 찍찍\uD83D\uDC39 ")
    private String description;

    @Schema(description = "난이도", example = "상")
    private String level;

    @Schema(description = "문제 개수(3~5)", example = "5")
    private int problemCount;

    @Schema(description = "시작 시각 (HH:mm 포맷)", example = "10:30")
    private String startTime;

    @Schema(description = "응시 시간", example = "3")
    private int durationTime;

    @Schema(description = "현재 팀원 수", example = "4")
    private int currentMembers;

    @Schema(description = "팀장 userID", example = "3")
    private long leaderId;

    @Schema(description = "전체 멤버 목록")
    private List<TeamMemberDto> members;


    public TeamMainDto(Team team, List<TeamMemberDto> members) {
        this.id = team.getId();
        this.teamName = team.getTeamName();
        this.description = team.getDescription();
        this.level = team.getLevel().name();
        this.problemCount = team.getProblemCount();
        this.startTime = team.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.durationTime = team.getDurationTime();
        this.currentMembers = team.getCurrentMembers();
        this.leaderId = team.getLeader().getId();
        this.members = members;
    }
}
