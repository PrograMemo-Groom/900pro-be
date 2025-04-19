package programo._pro.global.exception.chatException;

public class NotFoundChatException extends RuntimeException {

	public NotFoundChatException(String message) {
		super(message);
	}

	public static NotFoundChatException NotFoundChatRoomException() {
		return new NotFoundChatException("채팅방을 찾을 수 없습니다.");
	}

	public static NotFoundChatException NotFoundUserException() {
		return new NotFoundChatException("사용자를 찾을 수 없습니다.");
	}

	public static NotFoundChatException NotFoundChatbotException() {
		return new NotFoundChatException("챗봇 메시지를 찾을 수 없습니다.");
	}

}
