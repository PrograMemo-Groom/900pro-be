package programo._pro.global.exception.teamException;

public class NotJoinedTeamException extends RuntimeException {
    public NotJoinedTeamException() {
        super("이 팀의 팀원이 아닙니다.");
    }
}
