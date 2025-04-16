package programo._pro.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageResponse {
	private Long messageId;
	private Long chatRoomId;
	private Long userId;
	private String content;

	public ChatMessageResponse(Long messageId, Long chatRoomId, Long userId, String content) {
		this.messageId = messageId;
		this.chatRoomId = chatRoomId;
		this.userId = userId;
		this.content = content;
	}
}
