package programo._pro.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamCardDto {
    private Long teamId;
    private String teamName;
    private String level;
    private String startTime;
    private int problemCount;
    private int currentMembers;
}
