package programo._pro.dto;

import lombok.Getter;
import programo._pro.entity.Team;
import programo._pro.entity.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class TeamMainDto {

    private Long id;
    private String teamName;
    private String description;
    private String level;
    private int problemCount;
    private String startTime;
    private int durationTime;
    private int currentMembers;
    private long leaderId;
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
