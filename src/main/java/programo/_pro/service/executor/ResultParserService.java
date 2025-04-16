package programo._pro.service.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import programo._pro.dto.CodeExecutionResponse;
import programo._pro.dto.CodeExecutionResponse.ErrorInfo;

import java.util.Map;

@Slf4j
@Service
public class ResultParserService {

    private final ObjectMapper objectMapper;
    private final ErrorHandlingService errorHandlingService;

    public ResultParserService(ObjectMapper objectMapper, ErrorHandlingService errorHandlingService) {
        this.objectMapper = objectMapper;
        this.errorHandlingService = errorHandlingService;
    }

    /**
     * 실행 결과를 파싱하여 응답을 생성합니다.
     */
    public CodeExecutionResponse parseExecutionResult(String outputStr, int exitCode) {
        if (outputStr.startsWith("{") && outputStr.endsWith("}")) {
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
                return CodeExecutionResponse.builder()
                        .status("error")
                        .error(ErrorInfo.builder()
                                .code(ErrorType.SYSTEM_UNKNOWN_ERROR.getCode())
                                .message("결과 데이터 파싱 중 오류가 발생했습니다")
                                .detail(e.getMessage())
                                .source("system")
                                .build())
                        .stdout(outputStr)
                        .build();
            }
        } else {
            // 에러가 포함되어 있는지 확인
            if (exitCode != 0 || outputStr.contains("Error") || outputStr.contains("Exception")) {
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
     */
    private Map<String, Object> handleNestedJson(Map<String, Object> resultMap) {
        // stdout이 JSON 문자열인 경우(중첩된 JSON) 추가 처리
        if (resultMap.containsKey("stdout") && resultMap.get("stdout") != null) {
            String stdoutStr = resultMap.get("stdout").toString().trim();
            if (stdoutStr.startsWith("{") && stdoutStr.endsWith("}")) {
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

        // 에러 정보 변환
        ErrorInfo errorInfo = null;
        if (errorStr != null && !errorStr.isEmpty()) {
            errorInfo = errorHandlingService.handleExecutionError(errorStr);
        }

        return CodeExecutionResponse.builder()
                .status(resultMap.getOrDefault("status", "success").toString())
                .stdout(stdout)
                .error(errorInfo)
                .exitCode(exitCode)
                .build();
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
            return Integer.valueOf(exitCodeObj.toString());
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
