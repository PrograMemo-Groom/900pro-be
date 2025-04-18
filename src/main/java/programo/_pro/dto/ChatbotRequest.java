package programo._pro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "챗봇 메시지 데이터 전송 객체")
public class ChatbotRequest {

	@Schema(description = "팀 ID", example = "1", required = true)
	private Long teamId;

	@Schema(description = "테스트 날짜", example = "[2025-04-10]", required = true)
	private LocalDate testDate;

	@Schema(description = "챗봇 메시지 내용", example = "응시하느라 고생하셨습니다!", required = true)
	private String message;

	@Schema(description = "오늘의 문제 번호", example = "[111, 222, 333, 444]", required = true)
	private List<Integer> problemNumbers;

	@Schema(description = "메시지 전송 시간", example = "2025-04-10T15:44:00")
	private LocalDateTime sendAt;
}