package programo._pro.service.executor.languages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import programo._pro.dto.codeDto.CodeExecutionResponse;
import programo._pro.service.executor.AbstractExecutorService;
import programo._pro.service.executor.CodeExecutorProperties;
import programo._pro.service.executor.ErrorHandlingService;
import programo._pro.service.executor.ResultParserService;
import programo._pro.global.exception.codeException.ExecutorIOException;
import programo._pro.global.exception.codeException.ExecutorInterruptedException;
import programo._pro.global.exception.codeException.ExecutorTimeoutException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CppExecutorService extends AbstractExecutorService {

    private final String gccContainerName;
    private boolean gccContainerAvailable = false;
    private static final int MAX_CODE_SIZE = 1024 * 50; // 최대 50KB 코드 크기 제한

    public CppExecutorService(
            ResultParserService resultParserService,
            ErrorHandlingService errorHandlingService,
            CodeExecutorProperties properties) {
        super(resultParserService, errorHandlingService, properties);
        this.gccContainerName = properties.getContainer().getGccName();
    }

    @PostConstruct
    public void initialize() {
        gccContainerAvailable = checkContainerAvailability(gccContainerName);
        if (gccContainerAvailable) {
            log.info("C++ 코드 실행 컨테이너 초기화 완료: {}", gccContainerName);
        }
    }

    /**
     * C++ 코드를 실행하고 결과를 반환합니다.
     *
     * @param code 실행할 C++ 코드
     * @return 실행 결과(출력, 오류 등)
     */
    public CodeExecutionResponse executeCppCode(String code) {
        // 코드 크기 검증
        if (code.length() > MAX_CODE_SIZE) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleSizeExceededError("C++", MAX_CODE_SIZE))
                    .build();
        }

        // 컨테이너 실행 가능 여부 확인
        if (!ensureGccContainerAvailability()) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError(gccContainerName))
                    .build();
        }

        // 예외 처리는 AbstractExecutorService에서 처리
        return executeCode(code, gccContainerName);
    }

    /**
     * C++ 컨테이너 실행 가능 여부를 확인하고, 필요시 재확인합니다.
     */
    private boolean ensureGccContainerAvailability() {
        if (!gccContainerAvailable) {
            gccContainerAvailable = checkContainerAvailability(gccContainerName);
        }
        return gccContainerAvailable;
    }

    /**
     * C++ 컨테이너 내에서 코드를 실행합니다.
     */
    @Override
    protected ProcessResult executeCodeInContainer(String code)
            throws ExecutorIOException, ExecutorInterruptedException, ExecutorTimeoutException {
        Process process = null;
        try {
            // 도커 실행 명령 준비
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", "-i",
                gccContainerName,
                "/bin/bash", "-c", "head -c " + MAX_CODE_SIZE + " | /code/run.sh cpp"
            );

            // 표준 출력과 에러 출력을 분리:
            // 실행 상태 메시지는 stderr에 나오고 실제 프로그램 출력은 stdout에 나오므로,
            // redirectErrorStream을 false로 설정하여 각각 별도로 처리
            pb.redirectErrorStream(false);

            // 프로세스 실행 및 출력 캡처
            process = pb.start();

            // 코드를 표준 입력으로 전달
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(code.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }

            // 타임아웃 설정
            boolean completed = process.waitFor(properties.getTimeout().getCppExecution(), TimeUnit.SECONDS);

            // 표준 출력 읽기 (프로그램의 실제 출력)
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            }

            // 에러 출력 읽기 (상태 메시지와 컴파일/런타임 에러)
            StringBuilder stderr = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(
                            process.getErrorStream(),
                            StandardCharsets.UTF_8
                    )
            )) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            }

            // 타임아웃 확인
            if (!completed) {
                process.destroyForcibly();
                throw new ExecutorTimeoutException("C++ 코드 실행 시간이 초과되었습니다.");
            }

            // 상태 메시지 확인을 위해 stderr 로깅
            String stderrString = stderr.toString().trim();
            if (!stderrString.isEmpty()) {
                log.debug("C++ 실행 상태 및 에러 메시지: {}", stderrString);
            }

            // 컴파일/실행 중 에러가 발생했는지 확인
            if (process.exitValue() != 0) {
                log.error("C++ 코드 실행 실패: {}", stderrString);
                return new ProcessResult(stderrString, process.exitValue());
            }

            // 성공적인 경우 stdout만 결과로 반환
            return new ProcessResult(stdout.toString().trim(), process.exitValue());

        } catch (IOException e) {
            throw new ExecutorIOException("C++ 코드 실행 중 I/O 오류 발생", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecutorInterruptedException("C++ 코드 실행 중 인터럽트 발생", e);
        }
    }
}
