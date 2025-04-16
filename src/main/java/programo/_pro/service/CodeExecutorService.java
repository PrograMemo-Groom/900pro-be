package programo._pro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import programo._pro.dto.CodeExecutionResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class CodeExecutorService {

    private final ObjectMapper objectMapper;
    private final String containerName = "webide-code-executor";
    private boolean containerAvailable = false;

    public CodeExecutorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initialize() {
        checkContainerAvailability();
    }

    /**
     * 컨테이너 실행 가능 여부를 확인합니다.
     */
    private void checkContainerAvailability() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "ps",
                "--format", "{{.Names}}"
            );

            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && line.equals(containerName)) {
                        containerAvailable = true;
                        log.info("코드 실행 컨테이너 발견: {}", containerName);
                        break;
                    }
                }
            }

            process.waitFor(5, TimeUnit.SECONDS);

            if (!containerAvailable) {
                log.warn(
                        "코드 실행 컨테이너를 찾을 수 없습니다. " +
                        "컨테이너가 실행 중인지 확인하세요: {}",
                        containerName
                );
            } else {
                log.info("코드 실행 컨테이너 초기화 완료: {}", containerName);
            }
        } catch (IOException e) {
            log.error("컨테이너 확인 중 I/O 오류 발생", e);
        } catch (InterruptedException e) {
            log.error("컨테이너 확인 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Python 코드를 실행하고 결과를 반환합니다.
     *
     * @param code 실행할 Python 코드
     * @return 실행 결과(출력, 오류 등)
     */
    public CodeExecutionResponse executePythonCode(String code) {
        // 컨테이너 실행 가능 여부 확인
        if (!ensureContainerAvailability()) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(String.format(
                            "코드 실행 컨테이너(%s)가 실행 중이 아닙니다. " +
                            "Docker를 확인해주세요.",
                            containerName
                    ))
                    .build();
        }

        try {
            // 프로세스 실행 및 결과 획득
            ProcessResult processResult = executeCodeInContainer(code);

            // 결과 파싱 및 응답 생성
            return parseExecutionResult(processResult);
        } catch (IOException e) {
            log.error("코드 실행 중 I/O 오류 발생", e);
            return createErrorResponse("입출력 오류가 발생했습니다: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("코드 실행 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return createErrorResponse("코드 실행이 중단되었습니다: " + e.getMessage());
        } catch (TimeoutException e) {
            log.error("코드 실행 시간 초과", e);
            return createTimeoutResponse();
        } catch (Exception e) {
            log.error("코드 실행 중 예상치 못한 오류 발생", e);
            return createErrorResponse("코드 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 컨테이너 실행 가능 여부를 확인하고, 필요시 재확인합니다.
     */
    private boolean ensureContainerAvailability() {
        if (!containerAvailable) {
            checkContainerAvailability();
        }
        return containerAvailable;
    }

    /**
     * 컨테이너 내에서 코드를 실행합니다.
     */
    private ProcessResult executeCodeInContainer(String code)
            throws IOException, InterruptedException, TimeoutException {
        // 도커 실행 명령 준비
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "exec", "-i",
            containerName,
            "python3", "/code/run.py"
        );

        pb.redirectErrorStream(true);

        // 프로세스 실행 및 출력 캡처
        Process process = pb.start();

        // 코드를 표준 입력으로 전달
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(code.getBytes(StandardCharsets.UTF_8));
            stdin.flush();
        }

        // 타임아웃 설정
        boolean completed = process.waitFor(10, TimeUnit.SECONDS);

        // 결과 읽기
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // 타임아웃 확인
        if (!completed) {
            process.destroyForcibly();
            throw new TimeoutException("코드 실행 시간이 초과되었습니다.");
        }

        return new ProcessResult(output.toString().trim(), process.exitValue());
    }

    /**
     * 실행 결과를 파싱하여 응답을 생성합니다.
     */
    private CodeExecutionResponse parseExecutionResult(ProcessResult processResult) {
        String outputStr = processResult.getOutput();
        int exitCode = processResult.getExitCode();

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
                        .error(String.format(
                                "결과 데이터 파싱 중 오류가 발생했습니다: " +
                                "%s", e.getMessage()))
                        .stdout(outputStr)
                        .build();
            }
        } else {
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
        String error = extractMapValue(resultMap, "error");

        return CodeExecutionResponse.builder()
                .status(resultMap.getOrDefault("status", "success").toString())
                .stdout(stdout)
                .error(error)
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
    private CodeExecutionResponse createErrorResponse(String errorMessage) {
        return CodeExecutionResponse.builder()
                .status("error")
                .error(errorMessage)
                .build();
    }

    /**
     * 타임아웃 응답을 생성합니다.
     */
    private CodeExecutionResponse createTimeoutResponse() {
        return CodeExecutionResponse.builder()
                .status("timeout")
                .error("코드 실행 시간이 초과되었습니다. " +
                       "무한 루프나 과도한 연산이 있는지 확인해주세요.")
                .build();
    }

    /**
     * 프로세스 실행 결과를 담는 내부 클래스
     */
    private static class ProcessResult {
        private final String output;
        private final int exitCode;

        public ProcessResult(String output, int exitCode) {
            this.output = output;
            this.exitCode = exitCode;
        }

        public String getOutput() {
            return output;
        }

        public int getExitCode() {
            return exitCode;
        }
    }
}
