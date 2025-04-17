package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import programo._pro.entity.Chatbot;
import programo._pro.service.ChatbotService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatbotController {

	private final ChatbotService chatbotService;

	@GetMapping("/{teamId}")
	public List<Chatbot> getChatbotsByTeamId(@PathVariable Long teamId) {
		return chatbotService.getChatbotsByTeamId(teamId);
	}

	@PostMapping
	public void createChatbotMessage(@RequestBody Chatbot chatbot) {
		chatbotService.createChatbotMessage(chatbot);
	}
}
