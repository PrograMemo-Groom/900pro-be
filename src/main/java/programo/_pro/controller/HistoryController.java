package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.codeDto.CodeRequestDto;
import programo._pro.entity.Problem;
import programo._pro.global.ApiResponse;
import programo._pro.service.CodeService;
import programo._pro.service.HistoryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "히스토리 페이지", description = "히스토리 페이지 API")
public class HistoryController {
    private final HistoryService historyService;
    private final CodeService codeService;

    // test이벤트 id 값으로 문제 정보와 문제 정보 리스트를 조회 (UI 상 왼쪽 화면 첫 세팅 API)
    @Operation(summary = "테스트의 문제를 조회", description = "해당 팀의 해당 날짜 모든 문제 정보를 불러옵니다.")
    @GetMapping("/gethistory")
    public ResponseEntity<ApiResponse<List<Problem>>> getHistory(@Parameter(name = "teamId", example = "1") @PathParam("teamId") int teamId,
                                                                 @Parameter(name = "dateTime", example = "2025-04-24T15:00:00") @PathParam("dateTime") LocalDateTime dateTime) {
        List<Problem> history = historyService.getHistory(teamId, dateTime);

        return ResponseEntity.ok(ApiResponse.success(history, "성공"));
    }


    // 팀원의 문제 풀이 정보와 하이라이트 테이블을 조회
//     test_id 와 problem_id, user_id를 받아야함
    @Operation(summary = "팀원 문제 풀이, 하이라이트,메모 조회", description = "팀원의 문제 풀이와 하이라이트 정보를 조회합니다.")
    @GetMapping("/member/code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCodeMemberCodeAndHighlight(@RequestBody CodeRequestDto CodeRequestDto) {
        Map<String, Object> data =  codeService.getCodeMemberCodeAndHighlight(CodeRequestDto.getTestId(), CodeRequestDto.getProblemId(), CodeRequestDto.getUserId());

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
