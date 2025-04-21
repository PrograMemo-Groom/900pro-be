package programo._pro.global.exception.userException;

import lombok.Getter;

public class NotFoundUserException extends RuntimeException {
    public NotFoundUserException(String message) {
        super(message);
    }

    public static NotFoundUserException byEmail() {
        return new NotFoundUserException("이메일로 사용자를 찾을 수 없습니다.");
    }

    public static NotFoundUserException byId() {
        return new NotFoundUserException("ID로 사용자를 찾을 수 없습니다.");
    }
}
