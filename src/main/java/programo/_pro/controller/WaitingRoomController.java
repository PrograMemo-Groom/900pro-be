package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import programo._pro.dto.waitingRoomDto.ReadyMessageDto;
import programo._pro.service.WaitingRoomService;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/waiting-room")
public class WaitingRoomController {

    private final WaitingRoomService waitingRoomService;

    // GET : 대기실 처음 입장 시 초기 상태, 팀원 정보
    @GetMapping("/{teamId}")
    public ResponseEntity<List<ReadyMessageDto>> getTeamWaitingStatus(@PathVariable Long teamId) {
        List<ReadyMessageDto> result = waitingRoomService.getTeamWaitingStatus(teamId);
        return ResponseEntity.ok(result);
    }

    // WebSocket 메시지 수신 : 상태 전송 처리
    @MessageMapping("/waiting-room/ready")
    public void handleReady(@Payload ReadyMessageDto message) {
        waitingRoomService.broadcastReadyStatus(message);
    }
}
