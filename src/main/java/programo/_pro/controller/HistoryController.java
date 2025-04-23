package programo._pro.controller;

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
    @GetMapping("/gethistory")
    public ResponseEntity<ApiResponse<List<Problem>>> getHistory(@PathParam("testId") int testId) {
        List<Problem> history = historyService.getHistory(testId);

        return ResponseEntity.ok(ApiResponse.success(history, "성공"));
    }


    // 팀원의 문제 풀이 정보와 하이라이트 테이블을 조회
//     test_id 와 problem_id, user_id를 받아야함
    @PostMapping("/member/code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCodeMemberCodeAndHighlight(@RequestBody CodeRequestDto CodeRequestDto) {
        Map<String, Object> data =  codeService.getCodeMemberCodeAndHighlight(CodeRequestDto.getTest_id(), CodeRequestDto.getProblem_id(), CodeRequestDto.getUser_id());

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
