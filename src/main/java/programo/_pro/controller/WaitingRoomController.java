package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.ErrorResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.problemDto.ProblemGenerateRequestDto;
import programo._pro.dto.waitingRoomDto.ReadyMessageDto;
//import programo._pro.global.ApiResponse;
import programo._pro.service.WaitingRoomService;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/waiting-room")
public class WaitingRoomController {

    private final WaitingRoomService waitingRoomService;

    @Operation(
            summary = "대기실 초기 상태 조회",
            description = "대기실에 처음 입장했을 때, 해당 팀의 팀원 상태 리스트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팀원 상태 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/{teamId}")
    public ResponseEntity<List<ReadyMessageDto>> getTeamWaitingStatus(
            @Parameter(description = "조회할 팀 ID") @PathVariable Long teamId) {
        List<ReadyMessageDto> result = waitingRoomService.getTeamWaitingStatus(teamId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "팀원 준비 상태 실시간 전송",
            description = "프론트에서 WebSocket을 통해 팀원의 준비/대기 상태를 실시간으로 전송합니다. (MessageMapping)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 전송 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @MessageMapping("/waiting-room/ready")
    public void handleReady(@Payload ReadyMessageDto message) {
        waitingRoomService.broadcastReadyStatus(message);
    }


    // 스케쥴링 통해 해당 팀의 시험 시작 시간에 자동으로 랜덤 문제 조회 후 문제 세팅
    @PostMapping("/set-problem")
    public ResponseEntity<programo._pro.global.ApiResponse<Map<String, Object>>> SetRandomProblem
    (@RequestBody ProblemGenerateRequestDto requestDto) {
        Map<String, Object> data = waitingRoomService.SetRandomProblem(requestDto);

        return ResponseEntity.ok(programo._pro.global.ApiResponse
                .success(data, "성공적으로 랜덤 문제를 조회 후 DB에 등록하였습니다."));
    }
}
