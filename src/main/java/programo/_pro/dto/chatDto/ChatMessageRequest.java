package programo._pro.dto.chatDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import programo._pro.entity.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "채팅 메시지 전송 요청")
public class ChatMessageRequest {

	@Schema(description = "채팅방 ID", example = "1", required = true)
	private Long chatRoomId;

	@Schema(description = "보낸 사람 ID", example = "10", required = true)
	private Long userId;

	@Schema(description = "메시지 내용", example = "안녕하세요 팀원 여러분!", required = true)
	private String content;

	@Schema(description = "메시지 전송 시간", example = "2025-04-10T15:44:00")
	private LocalDateTime sendAt;

	public User getUser() {
		User user = new User();
		user.setId(userId);
		return user;
	}
}