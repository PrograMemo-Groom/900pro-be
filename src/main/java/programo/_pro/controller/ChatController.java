package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import programo._pro.dto.ChatMessageRequest;
import programo._pro.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@MessageMapping("/chat/message") // /pub/chat/message
	@Operation(
			summary = "채팅 메시지 전송",
			description = "사용자가 채팅방에 메시지를 전송하면 이를 처리하는 API입니다.",
			requestBody = @RequestBody(
					description = "전송할 채팅 메시지",
					required = true
			)
	)
	public void receiveMessage(@Payload ChatMessageRequest messageRequest) {
		chatService.handleChatMessage(messageRequest);
	}
}