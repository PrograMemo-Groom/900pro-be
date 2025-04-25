package programo._pro.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.codeDto.CodeExecutionRequest;
import programo._pro.dto.codeDto.CodeExecutionResponse;
import programo._pro.dto.codeDto.CodeRequestDto;
import programo._pro.dto.codeDto.UpdateCodeDto;
import programo._pro.entity.Code;
import programo._pro.entity.Problem;
import programo._pro.global.ApiResponse;
import programo._pro.service.CodeService;
import programo._pro.service.executor.CodeExecutorService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/code")
public class CodeController {
    private final CodeExecutorService codeExecutorService;
    private final CodeService codeService;

    // user_id, test_id를 입력받아 해당 유저의 문제풀이들을 시험완료 상태로 업데이트
    @PatchMapping("/update/end-coding")
    public ResponseEntity<ApiResponse<String>> updateSubmitCode(@RequestBody CodeRequestDto codeRequestDto) {
        codeService.updateSubmitCode(codeRequestDto);

        return ResponseEntity.ok(ApiResponse.success("해당 유저의 상태가 정상적으로 응시 완료 상태로 변경되었습니다."));
    }

    @PatchMapping("/update/submit")
    public ResponseEntity<ApiResponse<String>> updateCode(@RequestBody UpdateCodeDto updateCodeDto) {
        codeService.updateCode(updateCodeDto);

        return ResponseEntity.ok(ApiResponse.success("해당 유저의 코드가 정상적으로 시험완료 상태로 변경되었습니다."));

    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<ApiResponse<List<Problem>>> getProblemsByTestId(@PathVariable int testId) {
        List<Problem> testProblems = codeService.getProblemsByTestId(testId);

        return ResponseEntity.ok(ApiResponse.success(testProblems, "성공적으로 테스트의 문제들을 조회했습니다."));
    }


    @PostMapping("/execute/python")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executePythonCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executePythonCode(request.getCode());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/execute/javascript")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executeJavaScriptCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeJavaScriptCode(request.getCode());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/execute/java")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executeJavaCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeJavaCode(request.getCode());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/execute/cpp")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executeCppCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeCppCode(request.getCode());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/execute/c")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executeCCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeCCode(request.getCode());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
