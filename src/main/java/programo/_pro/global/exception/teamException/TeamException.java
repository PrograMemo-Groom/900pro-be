package programo._pro.global.exception.teamException;

public class TeamException extends RuntimeException{
    public TeamException(String message) {
        super(message);
    }

    public static TeamException NotJoinedTeamException() {
        return new TeamException("이 팀의 팀원이 아닙니다.");
    }

    public static TeamException NotFoundTeamException() {
        return new TeamException("해당 팀을 찾을 수 없습니다.");
    }

    public static TeamException AlreadyJoinedTeamException() {
        return new TeamException("이미 가입된 팀입니다.");
    }
}
