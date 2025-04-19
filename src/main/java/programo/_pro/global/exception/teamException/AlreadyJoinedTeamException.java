package programo._pro.global.exception.teamException;

public class AlreadyJoinedTeamException extends RuntimeException {
    public AlreadyJoinedTeamException() {
        super("이미 가입된 팀입니다.");
    }
}
