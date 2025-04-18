package programo._pro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import programo._pro.entity.User;
import programo._pro.entity.TeamMember;

@Getter
@AllArgsConstructor
public class TeamMemberDto {

    private Long userId;
    private String userName;
    private boolean isLeader;

    public static TeamMemberDto from(TeamMember teamMember, User user) {
        return new TeamMemberDto(
                user.getId(),
                user.getUsername(),
                teamMember.isLeader()
        );
    }
}
