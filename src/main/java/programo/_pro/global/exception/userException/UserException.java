package programo._pro.global.exception.userException;

public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public static UserException byEmail() {
        return new UserException("이메일로 사용자를 찾을 수 없습니다.");
    }

    public static UserException byId() {
        return new UserException("ID로 사용자를 찾을 수 없습니다.");
    }
}
