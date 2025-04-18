package programo._pro.global.exception;

public class ExecutorIOException extends RuntimeException {
    public ExecutorIOException(String message) {
        super(message);
    }

    public ExecutorIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
