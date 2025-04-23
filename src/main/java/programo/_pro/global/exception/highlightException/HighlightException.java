package programo._pro.global.exception.highlightException;

public class HighlightException extends RuntimeException {
    public HighlightException(String message) {
        super(message);
    }

    public static HighlightException NotFoundHighlightException() {
        return new HighlightException("이 코드의 하이라이트를 찾을 수 없습니다");
    }
}
