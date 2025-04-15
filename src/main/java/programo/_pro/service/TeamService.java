package programo._pro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import programo._pro.dto.TeamCardDto;
import programo._pro.entity.Team;
import programo._pro.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<TeamCardDto> getAllTeams() {
        List<Team> teams = teamRepository.findByIsActiveTrue();

        return teams.stream()
                .map(team -> TeamCardDto.builder()
                        .teamId(team.getId())
                        .teamName(team.getTeamName())
                        .level(team.getLevel().name())
                        .startTime(team.getStartTime().toString()) //포맷 변경 필요 !!!!!
                        .problemCount(team.getProblemCount())
                        .currentMembers(team.getCurrentMembers())
                        .build())
                .collect(Collectors.toList());
    }
}
