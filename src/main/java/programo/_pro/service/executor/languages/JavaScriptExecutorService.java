package programo._pro.service.executor.languages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import programo._pro.dto.CodeExecutionResponse;
import programo._pro.service.executor.AbstractExecutorService;
import programo._pro.service.executor.CodeExecutorProperties;
import programo._pro.service.executor.ErrorHandlingService;
import programo._pro.service.executor.ResultParserService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class JavaScriptExecutorService extends AbstractExecutorService {

    private final String javascriptContainerName;
    private boolean javascriptContainerAvailable = false;
    private static final int MAX_CODE_SIZE = 1024 * 50; // 최대 50KB 코드 크기 제한

    public JavaScriptExecutorService(
            ResultParserService resultParserService,
            ErrorHandlingService errorHandlingService,
            CodeExecutorProperties properties) {
        super(resultParserService, errorHandlingService, properties);
        this.javascriptContainerName = properties.getContainer().getJavascriptName();
    }

    @PostConstruct
    public void initialize() {
        javascriptContainerAvailable = checkContainerAvailability(javascriptContainerName);
        if (javascriptContainerAvailable) {
            log.info("JavaScript 코드 실행 컨테이너 초기화 완료: {}", javascriptContainerName);
        }
    }

    /**
     * JavaScript 코드를 실행하고 결과를 반환합니다.
     *
     * @param code 실행할 JavaScript 코드
     * @return 실행 결과(출력, 오류 등)
     */
    public CodeExecutionResponse executeJavaScriptCode(String code) {
        // 코드 크기 검증
        if (code.length() > MAX_CODE_SIZE) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleSizeExceededError("JavaScript", MAX_CODE_SIZE))
                    .build();
        }

        // 컨테이너 실행 가능 여부 확인
        if (!ensureJavaScriptContainerAvailability()) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError(javascriptContainerName))
                    .build();
        }

        return executeCode(code, javascriptContainerName);
    }

    /**
     * JavaScript 컨테이너 실행 가능 여부를 확인하고, 필요시 재확인합니다.
     */
    private boolean ensureJavaScriptContainerAvailability() {
        if (!javascriptContainerAvailable) {
            javascriptContainerAvailable = checkContainerAvailability(javascriptContainerName);
        }
        return javascriptContainerAvailable;
    }

    /**
     * JavaScript 컨테이너 내에서 코드를 실행합니다.
     */
    @Override
    protected ProcessResult executeCodeInContainer(String code)
            throws IOException, InterruptedException, TimeoutException {
        // 도커 실행 명령 준비
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "exec", "-i",
            javascriptContainerName,
            "/bin/bash", "-c", "head -c " + MAX_CODE_SIZE + " | node /code/run.js"
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
        boolean completed = process.waitFor(properties.getTimeout().getJavaScriptExecution(), TimeUnit.SECONDS);

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
}
