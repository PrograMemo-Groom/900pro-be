package programo._pro.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import programo._pro.dto.CodeExecutionResponse;

@Slf4j
@Service
public class CodeExecutorService {

    private final PythonExecutorService pythonExecutor;
    private final JavaExecutorService javaExecutor;

    public CodeExecutorService(
            PythonExecutorService pythonExecutor,
            JavaExecutorService javaExecutor) {
        this.pythonExecutor = pythonExecutor;
        this.javaExecutor = javaExecutor;
        log.info("코드 실행 서비스 초기화 완료");
    }

    /**
     * Python 코드를 실행하고 결과를 반환합니다.
     *
     * @param code 실행할 Python 코드
     * @return 실행 결과(출력, 오류 등)
     */
    public CodeExecutionResponse executePythonCode(String code) {
        return pythonExecutor.executePythonCode(code);
    }

    /**
     * Java 코드를 실행하고 결과를 반환합니다.
     *
     * @param code 실행할 Java 코드
     * @return 실행 결과(출력, 오류 등)
     */
    public CodeExecutionResponse executeJavaCode(String code) {
        return javaExecutor.executeJavaCode(code);
    }
}
