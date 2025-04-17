package programo._pro.global.exception;

public class ExecutorInterruptedException extends RuntimeException {
    public ExecutorInterruptedException(String message) {
        super(message);
    }

    public ExecutorInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
