package programo._pro.service.executor;

import lombok.extern.slf4j.Slf4j;
import programo._pro.dto.CodeExecutionResponse;
import programo._pro.global.exception.ExecutorIOException;
import programo._pro.global.exception.ExecutorInterruptedException;
import programo._pro.global.exception.ExecutorTimeoutException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 코드 실행 서비스의 공통 기능을 제공하는 추상 클래스
 */
@Slf4j
public abstract class AbstractExecutorService {

    protected final ResultParserService resultParserService;
    protected final ErrorHandlingService errorHandlingService;
    protected final CodeExecutorProperties properties;

    protected AbstractExecutorService(
            ResultParserService resultParserService,
            ErrorHandlingService errorHandlingService,
            CodeExecutorProperties properties) {
        this.resultParserService = resultParserService;
        this.errorHandlingService = errorHandlingService;
        this.properties = properties;
    }

    /**
     * 코드를 실행하고 결과를 반환합니다. 예외 발생 시 적절한 오류 응답을 반환합니다.
     */
    protected CodeExecutionResponse executeCode(String code, String containerName) {
        try {
            // 프로세스 실행 및 결과 획득
            ProcessResult processResult = executeCodeInContainer(code);

            // 결과 파싱 및 응답 생성
            return resultParserService.parseExecutionResult(processResult.getOutput(), processResult.getExitCode());
        } catch (ExecutorTimeoutException e) {
            log.warn("컨테이너 {}의 코드 실행 시간 초과: {}", containerName, e.getMessage());
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError("Execution Timeout: " + containerName + " - " + e.getMessage()))
                    .build();
        } catch (ExecutorIOException e) {
            log.error("컨테이너 {}의 코드 실행 중 I/O 오류 발생: {}", containerName, e.getMessage(), e);
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError("IO Error: " + containerName + " - " + e.getMessage()))
                    .build();
        } catch (ExecutorInterruptedException e) {
            log.warn("컨테이너 {}의 코드 실행 중 인터럽트 발생: {}", containerName, e.getMessage());
            Thread.currentThread().interrupt(); // 인터럽트 상태 유지
            return CodeExecutionResponse.builder()
                    .status("error")
                    .error(errorHandlingService.handleContainerError("Interrupted Error: " + containerName + " - " + e.getMessage()))
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

            process.waitFor(properties.getTimeout().getContainerCheck(), TimeUnit.SECONDS);
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
            throws ExecutorIOException, ExecutorInterruptedException, ExecutorTimeoutException;

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

/*
<핵심 고려 사항>
1.  `executeCodeInContainer`의 책임: 이 메소드는 Docker 컨테이너와 직접 상호작용하며 코드를 실행하는 저수준(low-level) 작업을 수행합니다. 이 과정에서는 예측 가능한 다양한 문제(네트워크 오류, 프로세스 인터럽트, 타임아웃 등)가 발생할 수 있습니다. 이 메소드는 이러한 문제 발생 시 상위 호출자에게 명확하게 실패 상황을 알려야 할 책임이 있습니다.
2.  `executeCode`의 책임: 이 메소드는 `executeCodeInContainer`를 호출하고, 그 결과를 받아 최종적인 `CodeExecutionResponse` 객체(성공 또는 실패)로 변환하는 고수준(high-level) 조율자 역할을 합니다. 즉, 코드 실행 과정에서 발생할 수 있는 예상된 문제들을 처리하고 사용자(또는 상위 서비스)에게 일관된 형식의 응답을 제공할 책임이 있습니다.

<전략>
1.  `executeCodeInContainer`는 `throws Executor...Exception`을 유지합니다.
    * 이 메소드에서 발생하는 `ExecutorIOException`, `ExecutorInterruptedException`, `ExecutorTimeoutException` 등은 코드 실행 과정에서 충분히 발생할 수 있는 예측 가능한 예외 상황입니다. Java의 Checked Exception 메커니즘을 활용하여 이러한 예외 타입을 명시적으로 선언(`throws`)하면, 컴파일 타임에 호출자(`executeCode`)가 이 예외들을 인지하고 반드시 처리하도록 강제할 수 있습니다. 이는 코드의 안정성을 높입니다.
2.  `executeCode`는 `throws` 절을 가지지 않고, 내부에서 `try-catch`로 예외를 처리합니다.
    * `executeCode` 메소드의 역할은 실행 결과를 정상적인 응답 객체(`CodeExecutionResponse`)로 변환하는 것입니다. 실행 중 예외가 발생했을 때, 이를 처리하여 `status="error"`와 상세 오류 정보가 포함된 `CodeExecutionResponse`를 반환하는 것은 이 메소드의 핵심 책임 중 하나입니다. 만약 `executeCode`가 예외를 다시 던진다면(`throws`), 예외 처리의 책임이 다시 상위 계층(`CodeExecutorService` 등)으로 넘어가게 되고, 각 언어별 서비스나 `CodeExecutorService`에서 중복된 `try-catch` 및 `CodeExecutionResponse` 생성 로직이 필요하게 될 수 있습니다.
*/
