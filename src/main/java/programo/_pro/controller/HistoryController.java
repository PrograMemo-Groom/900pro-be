package programo._pro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.codeDto.CodeRequestDto;
import programo._pro.entity.Code;
import programo._pro.entity.Problem;
import programo._pro.global.ApiResponse;
import programo._pro.repository.CodeRepository;
import programo._pro.service.CodeService;
import programo._pro.service.HistoryService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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


//     팀원 코드 클릭 시 첫번째 팀원의 문제풀이를 조회
    // test_id 와 problem_id를 받아야함
    @PostMapping("/first-member/code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFirstMemberCode(@RequestBody CodeRequestDto CodeRequestDto) {
        Map<String, Object> data =  codeService.getFirstMemberCode(CodeRequestDto.getTest_id(), CodeRequestDto.getProblem_id());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 문제 번호로 해당 문제의 정보를 조회합니다
//    @GetMapping("/getproblem")
//    public ResponseEntity<ApiResponse<Problem>> getProblem(@PathParam("problemId") int problemId) {
//        Problem problem = historyService.getProblem(problemId);
//
//        return ResponseEntity.ok(ApiResponse.success(problem, "문제 정보를 성공적으로 조회했습니다"));
//
//    }

//    // 팀원의 제출한 코드를 조회
//    @GetMapping("/getSubmitCode")
//    public ResponseEntity<ApiResponse<String>> getSubmitCode(@PathParam("submitCode") String submitCode) {
//
//    }
}
