package programo._pro.service;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import programo._pro.dto.TeamCardDto;
import programo._pro.dto.TeamCreateRequest;
import programo._pro.dto.TeamMainDto;
import programo._pro.dto.TeamMemberDto;
import programo._pro.entity.Team;
import programo._pro.entity.TeamMember;
import programo._pro.entity.User;
import programo._pro.global.exception.AlreadyJoinedTeamException;
import programo._pro.global.exception.NotFoundTeamException;
import programo._pro.global.exception.NotFoundUserException;
import programo._pro.global.exception.NotJoinedTeamException;
import programo._pro.repository.TeamRepository;
import programo._pro.repository.TeamMemberRepository;
import programo._pro.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

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


    @Transactional(readOnly = true)
    public TeamMainDto getTeamMain(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(NotFoundTeamException::new);

        // 해당 팀 팀원들만 조회
        List<TeamMember> teamMembers = teamMemberRepository.findByTeam_Id(teamId);

        List<TeamMemberDto> memberDtos = teamMembers.stream()
                .map(tm -> new TeamMemberDto(
                        tm.getUser().getId(),
                        tm.getUser().getUsername(),
                        tm.isLeader()
                ))
                .toList();

        return new TeamMainDto(team, memberDtos);
    }

    @Transactional
    public Long createTeam(TeamCreateRequest dto, Long userId) {
        // ( 로그인된 ) 유저 id 받기 / 인증구현 완료전까진 컨트롤러에서 @RequestParam로 받아와서 쓰겠습니다
        User loginedUser = userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);

        // 팀 생성
        Team team = Team.builder()
                .teamName(dto.getTeamName())
                .description(dto.getDescription())
                .level(dto.getLevel())
                .problemCount(dto.getProblemCount())
                .startTime(dto.getStartTime())
                .durationTime(dto.getDurationTime())
                .currentMembers(1)
                .leader(loginedUser)
                .isActive(true)
                .build();

        teamRepository.save(team);

        // 팀멤버에 추가, 팀장 등록
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(loginedUser)
                .isLeader(true)
                .build();

        teamMemberRepository.save(teamMember);

        // 팀id 반환
        return team.getId();
    }

    @Transactional
    public void updateTeam(Long teamId, TeamCreateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(NotFoundTeamException::new);
        team.updateInfo(request);
    }

    @Transactional
    public void joinTeam(Long teamId, Long userId) {
        if (teamMemberRepository.existsByUserId(userId)) {
            throw new AlreadyJoinedTeamException();
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(NotFoundTeamException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);

        team.setCurrentMembers(team.getCurrentMembers() + 1);

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .isLeader(false)
                .build();

        teamMemberRepository.save(teamMember);
    }

    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(NotFoundTeamException::new);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(NotJoinedTeamException::new);

        team.setCurrentMembers(team.getCurrentMembers() - 1);

        teamMemberRepository.delete(member);
    }
}
