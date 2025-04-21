package programo._pro.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import programo._pro.dto.teamDto.TeamCardDto;
import programo._pro.dto.teamDto.TeamCreateRequest;
import programo._pro.dto.teamDto.TeamMainDto;
import programo._pro.dto.teamDto.TeamMemberDto;
import programo._pro.entity.Team;
import programo._pro.entity.TeamMember;
import programo._pro.entity.User;
import programo._pro.global.exception.teamException.TeamException;
import programo._pro.global.exception.userException.UserException;
import programo._pro.repository.TeamRepository;
import programo._pro.repository.TeamMemberRepository;
import programo._pro.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import static programo._pro.global.exception.teamException.TeamException.AlreadyJoinedTeamException;


@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;


    // 팀 리스트 조회. 검색어, 난이도, 정렬 기준 적용 가능.
    @Transactional(readOnly = true)
    public List<TeamCardDto> getAllTeams(String keyword, String level, String sort) {
        List<Team> teams = teamRepository.findByIsActiveTrue();

        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            teams = teams.stream()
                    .filter(team ->
                            team.getTeamName().toLowerCase().contains(lowerKeyword)
                    )
                    .collect(Collectors.toList());
        }

        if (level != null && !level.equalsIgnoreCase("all")) {
            teams = teams.stream()
                    .filter(team -> team.getLevel().name().equalsIgnoreCase(level))
                    .collect(Collectors.toList());
        }

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


    // 팀의 상세 정보와 팀원 목록(권한 포함)을 조회
    @Transactional(readOnly = true)
    public TeamMainDto getTeamMain(Long teamId) {

        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new NotFoundTeamException("해당 팀을 찾을 수 없습니다."));
                .orElseThrow(TeamException::NotFoundTeamException);

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


    // 팀 생성
    @Transactional
    public Long createTeam(TeamCreateRequest dto, Long userId) {
        // ( 로그인된 ) 유저 id 받기 / 인증구현 완료전까진 컨트롤러에서 @RequestParam로 받아와서 쓰겠습니다
        User loginedUser = userRepository.findById(userId)
                .orElseThrow(UserException::byId);

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

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(loginedUser)
                .isLeader(true)
                .build();

        teamMemberRepository.save(teamMember);

        return team.getId();
    }


    // 팀 수정 (팀장 권한)
    @Transactional
    public void updateTeam(Long teamId, TeamCreateRequest request) {
        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new NotFoundTeamException("해당 팀을 찾을 수 없습니다."));
                    .orElseThrow(TeamException::NotFoundTeamException);
        team.updateInfo(request);
    }


    // 팀 삭제 (팀장 권한)
    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new NotFoundTeamException("해당 팀을 찾을 수 없습니다."));
                .orElseThrow(TeamException::NotFoundTeamException);
        // 팀에 속한 멤버 삭제
        teamMemberRepository.deleteByTeam_Id(teamId);

        // 팀 비활성화
        team.setNotActive();
    }


    // 팀원 내보내기 (팀장 권한)
    @Transactional
    public void kickMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamException::NotFoundTeamException);

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(TeamException::NotJoinedTeamException);

        teamMemberRepository.delete(teamMember);
        team.setCurrentMembers(team.getCurrentMembers() - 1);
    }


    // 팀 가입
    @Transactional
    public void joinTeam(Long teamId, Long userId) {
        if (teamMemberRepository.existsByUserId(userId)) {
//            throw new AlreadyJoinedTeamException("이미 가입된 팀입니다.");
            throw AlreadyJoinedTeamException();
        }

        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new NotFoundTeamException("해당 팀을 찾을 수 없습니다."));
                .orElseThrow(TeamException::NotFoundTeamException);

        User user = userRepository.findById(userId)
                .orElseThrow(UserException::byId);

        team.setCurrentMembers(team.getCurrentMembers() + 1);

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .isLeader(false)
                .build();

        teamMemberRepository.save(teamMember);
    }


    // 팀 탈퇴 (팀원 권한)
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                //.orElseThrow(() -> new NotFoundTeamException("해당 팀을 찾을 수 없습니다."));
                .orElseThrow(TeamException::NotFoundTeamException);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
//                .orElseThrow(() -> new NotJoinedTeamException("이 팀의 팀원이 아닙니다."));
                    .orElseThrow(TeamException::NotJoinedTeamException);

        team.setCurrentMembers(team.getCurrentMembers() - 1);

        teamMemberRepository.delete(member);
    }

}
