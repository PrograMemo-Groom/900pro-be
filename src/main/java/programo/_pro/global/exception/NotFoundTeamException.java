package programo._pro.global.exception;

public class NotFoundTeamException extends RuntimeException {
    public NotFoundTeamException() {
        super("해당 팀을 찾을 수 없습니다.");
    }
}
