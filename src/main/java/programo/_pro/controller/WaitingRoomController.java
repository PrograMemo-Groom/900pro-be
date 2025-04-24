package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.problemDto.ProblemGenerateRequestDto;
import programo._pro.dto.waitingRoomDto.ReadyMessageDto;
import programo._pro.global.ApiResponse;
import programo._pro.service.WaitingRoomService;
import java.util.List;
import java.util.Map;

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


    // 스케쥴링 통해 해당 팀의 시험 시작 시간에 자동으로 랜덤 문제 조회 후 문제 세팅
    @PostMapping("/set-problem")
    public ResponseEntity<ApiResponse<Map<String, Object>>> SetRandomProblem(@RequestBody ProblemGenerateRequestDto requestDto) {
        Map<String, Object> data = waitingRoomService.SetRandomProblem(requestDto);


        return ResponseEntity.ok(ApiResponse.success(data, "성공적으로 랜덤 문제를 조회 후 DB에 등록하였습니다."));
    }
}
