package programo._pro.global.exception;

public class NotFoundUserException extends RuntimeException {
    public NotFoundUserException() {
        super("해당 사용자를 찾을 수 없습니다.");
    }
}
