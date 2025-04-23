package programo._pro.global.exception.problemException;

public class ProblemException extends RuntimeException {
    public ProblemException(String message) {
        super(message);
    }

    public static ProblemException NotFoundProblemException() {
        return new ProblemException("문제를 찾을 수 없습니다.");
    }
}
