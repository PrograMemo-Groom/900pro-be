package programo._pro.controller;

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
	public void receiveMessage(@Payload ChatMessageRequest messageRequest) {
		chatService.handleChatMessage(messageRequest);
	}
}