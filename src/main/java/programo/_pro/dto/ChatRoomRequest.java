package programo._pro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "채팅방 생성 요청 데이터")
public class ChatRoomRequest {

	@Schema(description = "팀 ID", example = "1", required = true)
	private Long teamId;
}