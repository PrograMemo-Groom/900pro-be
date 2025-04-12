package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.ChatMessageRequest;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "채팅 관련 API")
public class ChatController {

	@PostMapping("/send")
	@Operation(summary = "채팅 메시지 전송", description = "팀 채팅방에 메시지를 전송합니다.")
	public ResponseEntity<String> sendMessage(@RequestBody ChatMessageRequest request) {
		// 실제 저장 로직은 생략 (예시용)
		return ResponseEntity.ok("메시지 전송 완료: " + request.getMessage());
	}

	@GetMapping("/team/{teamId}")
	@Operation(summary = "채팅 메시지 조회", description = "특정 팀의 전체 메시지를 조회합니다.")
	public ResponseEntity<String> getMessages(@PathVariable Long teamId) {
		// 실제 조회 로직은 생략 (예시용)
		return ResponseEntity.ok("팀 ID " + teamId + "의 메시지 목록");
	}
}