package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import programo._pro.dto.CodeExecutionRequest;
import programo._pro.dto.CodeExecutionResponse;
import programo._pro.service.executor.CodeExecutorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
public class CodeExecutorController {

    private final CodeExecutorService codeExecutorService;

    @PostMapping("/execute/python")
    public ResponseEntity<CodeExecutionResponse> executePythonCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executePythonCode(request.getCode());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/execute/javascript")
    public ResponseEntity<CodeExecutionResponse> executeJavaScriptCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeJavaScriptCode(request.getCode());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/execute/java")
    public ResponseEntity<CodeExecutionResponse> executeJavaCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeJavaCode(request.getCode());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/execute/cpp")
    public ResponseEntity<CodeExecutionResponse> executeCppCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeCppCode(request.getCode());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/execute/c")
    public ResponseEntity<CodeExecutionResponse> executeCCode(@RequestBody CodeExecutionRequest request) {
        CodeExecutionResponse result = codeExecutorService.executeCCode(request.getCode());
        return ResponseEntity.ok(result);
    }
}
