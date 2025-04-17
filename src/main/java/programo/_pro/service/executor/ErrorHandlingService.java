package programo._pro.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import programo._pro.dto.CodeExecutionResponse.ErrorInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * 코드 실행 중 발생하는 다양한 에러를 처리하는 서비스
 */
@Slf4j
@Service
public class ErrorHandlingService {

    // 에러 타입별 감지 패턴
    private final Map<ErrorType, Pattern> errorPatterns = new HashMap<>();

    /**
     * 생성자. 에러 감지를 위한 패턴을 초기화합니다.
     */
    public ErrorHandlingService() {
        // 구문 오류 패턴
        errorPatterns.put(ErrorType.EXECUTION_SYNTAX_ERROR,
                Pattern.compile("(?i)(SyntaxError|구문\\s*오류|syntax\\s*error|parse\\s*error|invalid\\s*syntax|문법\\s*오류)"));

        // 컴파일 오류 패턴
        errorPatterns.put(ErrorType.EXECUTION_COMPILE_ERROR,
                Pattern.compile("(?i)(compilation\\s*error|compiler\\s*error|컴파일\\s*오류|compile\\s*error|cannot\\s*find\\s*symbol)"));
    }

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
     * 코드 크기 제한 초과 에러를 처리합니다.
     */
    public ErrorInfo handleSizeExceededError(String language, int maxSize) {
        return ErrorInfo.builder()
                .code(ErrorType.CODE_SIZE_EXCEEDED.getCode())
                .message(ErrorType.CODE_SIZE_EXCEEDED.getDefaultMessage())
                .detail(String.format("%s 코드 크기가 최대 허용 크기(%dKB)를 초과했습니다.", language, maxSize / 1024))
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
     * 정교한 패턴 매칭을 통해 오류 유형을 식별합니다.
     */
    public ErrorInfo handleExecutionError(String errorOutput) {
        ErrorType errorType = determineErrorType(errorOutput);
        String source = "execution";

        return ErrorInfo.builder()
                .code(errorType.getCode())
                .message(errorType.getDefaultMessage())
                .detail(errorOutput)
                .source(source)
                .build();
    }

    /**
     * 에러 출력을 분석하여 에러 타입을 결정합니다.
     * @param errorOutput 에러 출력 문자열
     * @return 감지된 에러 타입
     */
    private ErrorType determineErrorType(String errorOutput) {
        if (errorOutput == null || errorOutput.isEmpty()) {
            return ErrorType.EXECUTION_RUNTIME_ERROR;
        }

        // 각 에러 타입에 대한 패턴 매칭 시도
        for (Map.Entry<ErrorType, Pattern> entry : errorPatterns.entrySet()) {
            if (entry.getValue().matcher(errorOutput).find()) {
                log.debug("에러 유형 감지: {}", entry.getKey());
                return entry.getKey();
            }
        }

        // 기본 에러 타입
        return ErrorType.EXECUTION_RUNTIME_ERROR;
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
