package programo._pro.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 전송 요청 DTO")
public class ChatMessageRequest {

	@Schema(description = "팀 ID", example = "1")
	private Long teamId;

	@Schema(description = "보낸 사람 ID", example = "10")
	private Long senderId;

	@Schema(description = "메시지 내용", example = "안녕하세요 팀원 여러분!")
	private String message;

	public String getMessage() {
		return "전송 완료";
	}
}