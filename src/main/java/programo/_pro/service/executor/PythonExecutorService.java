package programo._pro.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import programo._pro.dto.CodeExecutionResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class PythonExecutorService extends AbstractExecutorService {

    private final String pythonContainerName = "webide-python-executor";
    private boolean pythonContainerAvailable = false;

    public PythonExecutorService(
            ResultParserService resultParserService,
            ErrorHandlingService errorHandlingService) {
        super(resultParserService, errorHandlingService);
    }

    @PostConstruct
    public void initialize() {
        pythonContainerAvailable = checkContainerAvailability(pythonContainerName);
        if (pythonContainerAvailable) {
            log.info("파이썬 코드 실행 컨테이너 초기화 완료: {}", pythonContainerName);
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
        if (!ensurePythonContainerAvailability()) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError(pythonContainerName))
                    .build();
        }

        return executeCode(code, pythonContainerName, pythonContainerAvailable);
    }

    /**
     * 파이썬 컨테이너 실행 가능 여부를 확인하고, 필요시 재확인합니다.
     */
    private boolean ensurePythonContainerAvailability() {
        if (!pythonContainerAvailable) {
            pythonContainerAvailable = checkContainerAvailability(pythonContainerName);
        }
        return pythonContainerAvailable;
    }

    /**
     * 파이썬 컨테이너 내에서 코드를 실행합니다.
     */
    @Override
    protected ProcessResult executeCodeInContainer(String code)
            throws IOException, InterruptedException, TimeoutException {
        // 도커 실행 명령 준비
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "exec", "-i",
            pythonContainerName,
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
}
