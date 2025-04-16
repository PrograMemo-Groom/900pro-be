package programo._pro.service.executor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 코드 실행 관련 설정을 담는 클래스
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "code-executor")
public class CodeExecutorProperties {

    private Container container = new Container();
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Container {
        private String javaName = "webide-java-executor";  // 기본값
        private String pythonName = "webide-python-executor";  // 기본값
    }

    @Getter
    @Setter
    public static class Timeout {
        private int containerCheck = 5;  // 컨테이너 상태 확인 타임아웃(초) 기본값
        private int javaExecution = 15;  // Java 코드 실행 타임아웃(초) 기본값
        private int pythonExecution = 10;  // Python 코드 실행 타임아웃(초) 기본값
    }
}
