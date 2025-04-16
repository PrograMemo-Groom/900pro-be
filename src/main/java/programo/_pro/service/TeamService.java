package programo._pro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import programo._pro.dto.TeamCardDto;
import programo._pro.entity.Team;
import programo._pro.repository.TeamRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<TeamCardDto> getAllTeams(String keyword, String level, String sort) {
        List<Team> teams = teamRepository.findByIsActiveTrue();

        // 키워드 필터링 (팀이름에 포함되는 경우), 대소문자 구분없이 검색가능
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            teams = teams.stream()
                    .filter(team ->
                            team.getTeamName().toLowerCase().contains(lowerKeyword)
                    )
                    .collect(Collectors.toList());
        }

        // 난이도 선택 안할경우 all이 디폴트
        if (level != null && !level.equalsIgnoreCase("all")) {
            teams = teams.stream()
                    .filter(team -> team.getLevel().name().equalsIgnoreCase(level))
                    .collect(Collectors.toList());
        }

        // 정렬 선택 안할경우 최신순이 디폴트
        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("createdAt")) {
            teams.sort(Comparator.comparing(Team::getCreatedAt).reversed());
        } else if ("problemCount".equals(sort)) {
            teams.sort(Comparator.comparing(Team::getProblemCount).reversed());
        } else if ("name".equals(sort)) {
            teams.sort(Comparator.comparing(Team::getTeamName));
        } else if ("currentMembers".equals(sort)) {
            teams.sort(Comparator.comparing(Team::getCurrentMembers).reversed());
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return teams.stream()
                .map(team -> TeamCardDto.builder()
                        .teamId(team.getId())
                        .teamName(team.getTeamName())
                        .level(team.getLevel().name())
                        .startTime(team.getStartTime().format(timeFormatter))
                        .problemCount(team.getProblemCount())
                        .currentMembers(team.getCurrentMembers())
                        .build())
                .collect(Collectors.toList());
    }
}
