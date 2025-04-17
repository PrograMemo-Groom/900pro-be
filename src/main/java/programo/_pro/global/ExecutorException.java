package programo._pro.global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import programo._pro.dto.CodeExecutionResponse;
import programo._pro.global.exception.ExecutorIOException;
import programo._pro.global.exception.ExecutorInterruptedException;
import programo._pro.global.exception.ExecutorTimeoutException;
import programo._pro.service.executor.ErrorHandlingService;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExecutorException {

    private final ErrorHandlingService errorHandlingService;

    @ExceptionHandler(ExecutorTimeoutException.class)
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> handleExecutorTimeoutException(ExecutorTimeoutException e) {
        log.error("코드 실행 시간 초과 발생 (Executor): {}", e.getMessage(), e);
        CodeExecutionResponse response = CodeExecutionResponse.builder()
                .status("error")
                .error(errorHandlingService.handleSystemException(e))
                .build();
        String errorMessage = "코드 실행 시간이 초과되었습니다: " + e.getMessage();
        return ResponseEntity.ok(ApiResponse.fail(response, errorMessage));
    }

    @ExceptionHandler(ExecutorIOException.class)
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> handleExecutorIOException(ExecutorIOException e) {
        log.error("코드 실행 중 IO 예외 발생 (Executor): {}", e.getMessage(), e);
        CodeExecutionResponse response = CodeExecutionResponse.builder()
                .status("error")
                .error(errorHandlingService.handleSystemException(e))
                .build();
        String errorMessage = "코드 실행 중 입출력 오류가 발생했습니다: " + e.getMessage();
        return ResponseEntity.ok(ApiResponse.fail(response, errorMessage));
    }

    @ExceptionHandler(ExecutorInterruptedException.class)
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> handleExecutorInterruptedException(ExecutorInterruptedException e) {
        log.error("코드 실행 중 인터럽트 예외 발생 (Executor): {}", e.getMessage(), e);
        Thread.currentThread().interrupt(); // Preserve interrupt status
        CodeExecutionResponse response = CodeExecutionResponse.builder()
                .status("error")
                .error(errorHandlingService.handleSystemException(e))
                .build();
        String errorMessage = "코드 실행이 중단되었습니다: " + e.getMessage();
        return ResponseEntity.ok(ApiResponse.fail(response, errorMessage));
    }
}
