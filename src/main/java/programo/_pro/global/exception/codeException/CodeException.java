package programo._pro.global.exception.codeException;

public class CodeException extends RuntimeException {
    public CodeException(String message) {
        super(message);
    }

    public static CodeException NotFoundCodeException() {
      return new CodeException("풀이를 조회할 수 없습니다");
    }
    public static CodeException NotFoundCodeHighlightException() {
        return new CodeException("하이라이트 정보를 가져올 수 없습니다.");

    }
}
