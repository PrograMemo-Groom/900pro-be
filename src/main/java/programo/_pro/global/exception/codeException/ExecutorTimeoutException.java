package programo._pro.global.exception.codeException;

public class ExecutorTimeoutException extends RuntimeException {
    public ExecutorTimeoutException(String message) {
        super(message);
    }

    public ExecutorTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
