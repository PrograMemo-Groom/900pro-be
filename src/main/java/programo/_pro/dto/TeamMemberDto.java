package programo._pro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import programo._pro.entity.User;
import programo._pro.entity.TeamMember;

@Getter
@AllArgsConstructor
public class TeamMemberDto {

    @Schema(description = "팀원 ID", example = "3")
    private Long userId;

    @Schema(description = "사용자 이름", example = "거녕거녕")
    private String userName;

    @Schema(description = "팀장 여부", example = "true")
    private boolean isLeader;

    public static TeamMemberDto from(TeamMember teamMember, User user) {
        return new TeamMemberDto(
                user.getId(),
                user.getUsername(),
                teamMember.isLeader()
        );
    }
}
