package programo._pro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import programo._pro.dto.waitingRoomDto.ReadyMessageDto;
import programo._pro.entity.TeamMember;
import programo._pro.repository.TeamMemberRepository;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WaitingRoomService {

    private final TeamMemberRepository teamMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 대기실 처음 입장 시 초기 상태, 팀원 정보
    public List<ReadyMessageDto> getTeamWaitingStatus(Long teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeam_Id(teamId);

        return teamMembers.stream()
                .map(tm -> new ReadyMessageDto(
                        teamId,
                        tm.getUser().getId(),
                        tm.getUser().getUsername(),
                        "WAITING"
                ))
                .collect(Collectors.toList());
    }

    // 준비 상태 전송 처리 (WebSocket)
    public void broadcastReadyStatus(ReadyMessageDto message) {
        String topic = "/sub/waiting-room/" + message.getTeamId();
        messagingTemplate.convertAndSend(topic, message);
    }
}
