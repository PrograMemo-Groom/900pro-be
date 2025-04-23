package programo._pro.global.exception.testException;

public class TestException extends RuntimeException {
    public TestException(String message) {
        super(message);
    }

    public static TestException NotFoundTestException(String testId) {
        return new TestException("테스트를 찾을 수 없습니다.");
    }
}
