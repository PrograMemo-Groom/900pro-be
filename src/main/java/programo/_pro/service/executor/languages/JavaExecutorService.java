package programo._pro.service.executor.languages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import programo._pro.dto.codeDto.CodeExecutionResponse;
import programo._pro.global.exception.codeException.ExecutorIOException;
import programo._pro.global.exception.codeException.ExecutorInterruptedException;
import programo._pro.global.exception.codeException.ExecutorTimeoutException;
import programo._pro.service.executor.AbstractExecutorService;
import programo._pro.service.executor.CodeExecutorProperties;
import programo._pro.service.executor.ErrorHandlingService;
import programo._pro.service.executor.ResultParserService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JavaExecutorService extends AbstractExecutorService {

    private final String javaContainerName;
    private boolean javaContainerAvailable = false;
    private static final int MAX_CODE_SIZE = 1024 * 50; // 최대 50KB 코드 크기 제한

    public JavaExecutorService(
            ResultParserService resultParserService,
            ErrorHandlingService errorHandlingService,
            CodeExecutorProperties properties) {
        super(resultParserService, errorHandlingService, properties);
        this.javaContainerName = properties.getContainer().getJavaName();
    }

    @PostConstruct
    public void initialize() {
        javaContainerAvailable = checkContainerAvailability(javaContainerName);
        if (javaContainerAvailable) {
            log.info("Java 코드 실행 컨테이너 초기화 완료: {}", javaContainerName);
        }
    }

    /**
     * Java 코드를 실행하고 결과를 반환합니다.
     *
     * @param code 실행할 Java 코드
     * @return 실행 결과(출력, 오류 등)
     */
    public CodeExecutionResponse executeJavaCode(String code) {
        // 코드 크기 검증
        if (code.length() > MAX_CODE_SIZE) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleSizeExceededError("Java", MAX_CODE_SIZE))
                    .build();
        }

        // 컨테이너 실행 가능 여부 확인
        if (!ensureJavaContainerAvailability()) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError(javaContainerName))
                    .build();
        }

        // 예외 처리는 AbstractExecutorService에서 처리
        return executeCode(code, javaContainerName);
    }

    /**
     * Java 컨테이너 실행 가능 여부를 확인하고, 필요시 재확인합니다.
     */
    private boolean ensureJavaContainerAvailability() {
        if (!javaContainerAvailable) {
            javaContainerAvailable = checkContainerAvailability(javaContainerName);
        }
        return javaContainerAvailable;
    }

    /**
     * Java 컨테이너 내에서 코드를 실행합니다.
     */
    @Override
    protected ProcessResult executeCodeInContainer(String code)
            throws ExecutorIOException, ExecutorInterruptedException, ExecutorTimeoutException {
        try {
            // 도커 실행 명령 준비
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", "-i",
                javaContainerName,
                "/bin/bash", "-c", "head -c " + MAX_CODE_SIZE + " | /code/run.sh"
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
            boolean completed = process.waitFor(properties.getTimeout().getJavaExecution(), TimeUnit.SECONDS);

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
                throw new ExecutorTimeoutException("코드 실행 시간이 초과되었습니다.");
            }

            return new ProcessResult(output.toString().trim(), process.exitValue());
        } catch (IOException e) {
            throw new ExecutorIOException("코드 실행 중 I/O 오류가 발생했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecutorInterruptedException("코드 실행이 인터럽트되었습니다.", e);
        }
    }
}
