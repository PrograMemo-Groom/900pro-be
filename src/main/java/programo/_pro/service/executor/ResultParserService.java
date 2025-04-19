package programo._pro.service.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import programo._pro.dto.codeDto.CodeExecutionResponse;
import programo._pro.dto.codeDto.CodeExecutionResponse.ErrorInfo;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ResultParserService {

    private final ObjectMapper objectMapper;
    private final ErrorHandlingService errorHandlingService;
    // 에러 문자열을 감지하기 위한 패턴 - 대소문자 구분 없이 검색
    private final Pattern errorPattern = Pattern.compile("(?i)(error|exception|traceback|fault|failure)");

    public ResultParserService(ObjectMapper objectMapper, ErrorHandlingService errorHandlingService) {
        this.objectMapper = objectMapper;
        this.errorHandlingService = errorHandlingService;
    }

    /**
     * 실행 결과를 파싱하여 응답을 생성합니다.
     * @param outputStr 코드 실행 출력 문자열
     * @param exitCode 프로세스 종료 코드
     * @return 코드 실행 응답 객체
     */
    public CodeExecutionResponse parseExecutionResult(String outputStr, int exitCode) {
        // JSON 형식인지 확인
        if (isValidJson(outputStr)) {
            try {
                // JSON 파싱 시도
                Map<String, Object> resultMap = parseJsonOutput(outputStr);

                // 중첩된 JSON 처리
                resultMap = handleNestedJson(resultMap);

                // 응답 구성
                return buildResponseFromMap(resultMap, exitCode);
            } catch (JsonProcessingException e) {
                log.error("JSON 파싱 오류", e);
                // JSON 파싱에 실패한 경우 원본 출력 반환
                return createParsingErrorResponse(outputStr, e);
            }
        } else {
            // 에러가 포함되어 있는지 확인
            if (exitCode != 0 || containsErrorKeywords(outputStr)) {
                return CodeExecutionResponse.builder()
                        .status("error")
                        .error(errorHandlingService.handleExecutionError(outputStr))
                        .stdout(outputStr)
                        .exitCode(exitCode)
                        .build();
            }

            return CodeExecutionResponse.builder()
                    .status("success")
                    .stdout(outputStr)
                    .exitCode(exitCode)
                    .build();
        }
    }

    /**
     * 문자열이 유효한 JSON인지 검증합니다.
     * @param str 검증할 문자열
     * @return 유효한 JSON이면 true, 아니면 false
     */
    private boolean isValidJson(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        String trimmed = str.trim();
        // 간단한 첫 체크: '{' 로 시작하고 '}'로 끝나는지 확인
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            return false;
        }

        try {
            objectMapper.readTree(trimmed);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 출력 문자열에 오류를 나타내는 키워드가 있는지 확인합니다.
     * @param output 검사할 출력 문자열
     * @return 오류 키워드가 포함되어 있으면 true
     */
    private boolean containsErrorKeywords(String output) {
        if (output == null || output.isEmpty()) {
            return false;
        }
        return errorPattern.matcher(output).find();
    }

    /**
     * JSON 파싱 오류 응답을 생성합니다.
     * @param originalOutput 원본 출력 문자열
     * @param exception 발생한 예외
     * @return 파싱 오류 응답
     */
    private CodeExecutionResponse createParsingErrorResponse(String originalOutput, Exception exception) {
        return CodeExecutionResponse.builder()
                .status("error")
                .error(ErrorInfo.builder()
                        .code(ErrorType.SYSTEM_UNKNOWN_ERROR.getCode())
                        .message("결과 데이터 파싱 중 오류가 발생했습니다")
                        .detail(exception.getMessage())
                        .source("system")
                        .build())
                .stdout(originalOutput)
                .build();
    }

    /**
     * JSON 출력을 파싱합니다.
     */
    private Map<String, Object> parseJsonOutput(String jsonStr)
            throws JsonProcessingException {
        return objectMapper.readValue(
                jsonStr,
                new TypeReference<Map<String, Object>>() {}
        );
    }

    /**
     * 중첩된 JSON을 처리합니다.
     * 이 메소드는 stdout 필드가 JSON 형식 문자열인 경우, 중첩된 JSON을 파싱하여 반환합니다.
     * 컨테이너 실행 스크립트가 표준 출력을 JSON으로 래핑하는 경우를 처리합니다.
     */
    private Map<String, Object> handleNestedJson(Map<String, Object> resultMap) {
        // stdout이 JSON 문자열인 경우(중첩된 JSON) 추가 처리
        if (resultMap.containsKey("stdout") && resultMap.get("stdout") != null) {
            String stdoutStr = resultMap.get("stdout").toString().trim();
            if (isValidJson(stdoutStr)) {
                try {
                    // 중첩된 JSON 파싱 시도
                    Map<String, Object> nestedMap = parseJsonOutput(stdoutStr);
                    // 중첩된 JSON이 유효한 경우 내부 결과로 대체
                    log.info("중첩된 JSON 결과를 파싱했습니다.");
                    return nestedMap;
                } catch (JsonProcessingException e) {
                    // 파싱 실패 시 원래 값 유지
                    log.warn("중첩된 JSON 파싱 실패: {}", e.getMessage());
                }
            }
        }
        return resultMap;
    }

    /**
     * 결과 맵에서 응답 객체를 생성합니다.
     */
    private CodeExecutionResponse buildResponseFromMap(
            Map<String, Object> resultMap,
            int defaultExitCode) {
        // exit_code 처리
        Integer exitCode = extractExitCode(resultMap, defaultExitCode);

        // stdout과 error 필드 가져오기
        String stdout = extractMapValue(resultMap, "stdout");
        String errorStr = extractMapValue(resultMap, "error");

        // 응답 상태 결정 (error 필드가 있거나 exit_code가 0이 아니면 error)
        String status = determineStatus(resultMap, errorStr, exitCode);

        // 에러 정보 변환
        ErrorInfo errorInfo = null;
        if (errorStr != null && !errorStr.isEmpty()) {
            errorInfo = errorHandlingService.handleExecutionError(errorStr);
        }

        return CodeExecutionResponse.builder()
                .status(status)
                .stdout(stdout)
                .error(errorInfo)
                .exitCode(exitCode)
                .build();
    }

    /**
     * 결과 상태를 결정합니다.
     */
    private String determineStatus(Map<String, Object> resultMap, String errorStr, Integer exitCode) {
        // 명시적으로 상태가 지정된 경우 그대로 사용
        if (resultMap.containsKey("status")) {
            return resultMap.get("status").toString();
        }

        // 에러가 있거나 종료 코드가 0이 아니면 error
        if ((errorStr != null && !errorStr.isEmpty()) || (exitCode != null && exitCode != 0)) {
            return "error";
        }

        // 그 외에는 성공
        return "success";
    }

    /**
     * 맵에서 종료 코드를 추출합니다.
     */
    private Integer extractExitCode(Map<String, Object> resultMap, int defaultExitCode) {
        if (!resultMap.containsKey("exit_code")) {
            return defaultExitCode;
        }

        Object exitCodeObj = resultMap.get("exit_code");
        if (exitCodeObj == null) {
            return defaultExitCode;
        }

        if (exitCodeObj instanceof Integer) {
            return (Integer) exitCodeObj;
        } else {
            try {
                return Integer.valueOf(exitCodeObj.toString());
            } catch (NumberFormatException e) {
                log.warn("잘못된 exit_code 형식: {}", exitCodeObj);
                return defaultExitCode;
            }
        }
    }

    /**
     * 맵에서 문자열 값을 추출합니다.
     */
    private String extractMapValue(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            return map.get(key).toString();
        }
        return null;
    }

    /**
     * 오류 응답을 생성합니다.
     */
    public CodeExecutionResponse createErrorResponse(String errorMessage) {
        return CodeExecutionResponse.builder()
                .status("error")
                .error(ErrorInfo.builder()
                        .code(ErrorType.SYSTEM_UNKNOWN_ERROR.getCode())
                        .message(ErrorType.SYSTEM_UNKNOWN_ERROR.getDefaultMessage())
                        .detail(errorMessage)
                        .source("system")
                        .build())
                .build();
    }

    /**
     * 타임아웃 응답을 생성합니다.
     */
    public CodeExecutionResponse createTimeoutResponse() {
        return CodeExecutionResponse.builder()
                .status("timeout")
                .error(errorHandlingService.createTimeoutError())
                .build();
    }
}
