package programo._pro.global.exception;

public class AlreadyJoinedTeamException extends RuntimeException {
    public AlreadyJoinedTeamException() {
        super("이미 가입된 팀입니다.");
    }
}
