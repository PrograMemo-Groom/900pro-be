package programo._pro.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageResponse {
	private String content;
	private LocalDateTime sendAt;
	private String userName;

	public ChatMessageResponse(String content, LocalDateTime sendAt, String userName) {
		this.content = content;
		this.sendAt = sendAt;
		this.userName = userName;
	}
}
