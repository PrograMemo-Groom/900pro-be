package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import programo._pro.dto.ChatMessageRequest;
import programo._pro.service.ChatService;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 메시지 전송 API")
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;

	@Operation(
			summary = "채팅 메시지 전송",
			description = "사용자가 채팅방에 메시지를 전송하면 이를 처리하는 API입니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: 요청 파라미터 오류"),
					@ApiResponse(responseCode = "500", description = "서버 오류: 메시지 전송 처리 중 문제가 발생했습니다.")
			})
	@PostMapping("/message")
	public void receiveMessage(
			@Parameter(description = "채팅 메시지 요청 정보")
			@RequestBody ChatMessageRequest messageRequest) {
		chatService.handleChatMessage(messageRequest);
	}

	@Operation(
			summary = "WebSocket 메시지 수신",
			description = "WebSocket을 통해 채팅방에서 메시지를 수신합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "메시지 수신 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: WebSocket 메시지 수신 오류"),
					@ApiResponse(responseCode = "500", description = "서버 오류: 메시지 수신 처리 중 문제가 발생했습니다.")
			})
	@MessageMapping("/chat/message") // /pub/chat/message
	public void receiveWebSocketMessage(@Payload ChatMessageRequest messageRequest) {
		chatService.handleChatMessage(messageRequest);
	}
}