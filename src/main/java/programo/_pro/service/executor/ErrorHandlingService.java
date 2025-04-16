package programo._pro.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import programo._pro.dto.CodeExecutionResponse.ErrorInfo;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 코드 실행 중 발생하는 다양한 에러를 처리하는 서비스
 */
@Slf4j
@Service
public class ErrorHandlingService {

    /**
     * 컨테이너 관련 에러를 처리합니다.
     */
    public ErrorInfo handleContainerError(String containerName) {
        return ErrorInfo.builder()
                .code(ErrorType.CONTAINER_NOT_AVAILABLE.getCode())
                .message(ErrorType.CONTAINER_NOT_AVAILABLE.getDefaultMessage())
                .detail(String.format("%s 컨테이너가 실행 중이 아닙니다. Docker를 확인해주세요.", containerName))
                .source("system")
                .build();
    }

    /**
     * 시스템 예외를 적절한 에러 정보로 변환합니다.
     */
    public ErrorInfo handleSystemException(Exception e) {
        ErrorType errorType;
        String source = "system";

        if (e instanceof TimeoutException) {
            errorType = ErrorType.CONTAINER_TIMEOUT;
        } else if (e instanceof IOException) {
            errorType = ErrorType.SYSTEM_IO_ERROR;
        } else if (e instanceof InterruptedException) {
            errorType = ErrorType.SYSTEM_INTERRUPTED;
        } else {
            errorType = ErrorType.SYSTEM_UNKNOWN_ERROR;
        }

        return ErrorInfo.builder()
                .code(errorType.getCode())
                .message(errorType.getDefaultMessage())
                .detail(e.getMessage())
                .source(source)
                .build();
    }

    /**
     * 실행 결과 출력에서 에러 타입을 감지하고 처리합니다.
     */
    public ErrorInfo handleExecutionError(String errorOutput) {
        ErrorType errorType;
        String source = "execution";

        if (errorOutput.contains("SyntaxError") || errorOutput.contains("구문 오류")) {
            errorType = ErrorType.EXECUTION_SYNTAX_ERROR;
        } else if (errorOutput.contains("compilation error") || errorOutput.contains("컴파일 오류")) {
            errorType = ErrorType.EXECUTION_COMPILE_ERROR;
        } else {
            errorType = ErrorType.EXECUTION_RUNTIME_ERROR;
        }

        return ErrorInfo.builder()
                .code(errorType.getCode())
                .message(errorType.getDefaultMessage())
                .detail(errorOutput)
                .source(source)
                .build();
    }

    /**
     * 시간 초과 에러 정보를 생성합니다.
     */
    public ErrorInfo createTimeoutError() {
        return ErrorInfo.builder()
                .code(ErrorType.CONTAINER_TIMEOUT.getCode())
                .message(ErrorType.CONTAINER_TIMEOUT.getDefaultMessage())
                .detail("코드 실행 시간이 초과되었습니다. 무한 루프나 과도한 연산이 있는지 확인해주세요.")
                .source("execution")
                .build();
    }
}
