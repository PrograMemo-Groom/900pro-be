package programo._pro.service.executor;

import lombok.extern.slf4j.Slf4j;
import programo._pro.dto.CodeExecutionResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 코드 실행 서비스의 공통 기능을 제공하는 추상 클래스
 */
@Slf4j
public abstract class AbstractExecutorService {

    protected final ResultParserService resultParserService;
    protected final ErrorHandlingService errorHandlingService;

    protected AbstractExecutorService(
            ResultParserService resultParserService,
            ErrorHandlingService errorHandlingService) {
        this.resultParserService = resultParserService;
        this.errorHandlingService = errorHandlingService;
    }

    /**
     * 코드를 실행하고 결과를 반환합니다.
     */
    protected CodeExecutionResponse executeCode(String code, String containerName, boolean containerAvailable) {
        // 컨테이너 실행 가능 여부 확인
        if (!containerAvailable) {
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError(containerName))
                    .build();
        }

        try {
            // 프로세스 실행 및 결과 획득
            ProcessResult processResult = executeCodeInContainer(code);

            // 결과 파싱 및 응답 생성
            return resultParserService.parseExecutionResult(processResult.getOutput(), processResult.getExitCode());
        } catch (IOException e) {
            log.error("코드 실행 중 I/O 오류 발생", e);
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleSystemException(e))
                    .build();
        } catch (InterruptedException e) {
            log.error("코드 실행 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleSystemException(e))
                    .build();
        } catch (TimeoutException e) {
            log.error("코드 실행 시간 초과", e);
            return resultParserService.createTimeoutResponse();
        } catch (Exception e) {
            log.error("코드 실행 중 예상치 못한 오류 발생", e);
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleSystemException(e))
                    .build();
        }
    }

    /**
     * 컨테이너 실행 가능 여부를 확인합니다.
     */
    protected boolean checkContainerAvailability(String containerName) {
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
                        log.info("코드 실행 컨테이너 발견: {}", containerName);
                        return true;
                    }
                }
            }

            process.waitFor(5, TimeUnit.SECONDS);
            log.warn("코드 실행 컨테이너를 찾을 수 없습니다. 컨테이너가 실행 중인지 확인하세요: {}", containerName);
            return false;
        } catch (IOException e) {
            log.error("컨테이너 확인 중 I/O 오류 발생", e);
            return false;
        } catch (InterruptedException e) {
            log.error("컨테이너 확인 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 컨테이너 내에서 코드를 실행합니다.
     * 구체적인 실행 방법은 하위 클래스에서 구현합니다.
     */
    protected abstract ProcessResult executeCodeInContainer(String code)
            throws IOException, InterruptedException, TimeoutException;

    /**
     * 프로세스 실행 결과를 담는 내부 클래스
     */
    protected static class ProcessResult {
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
