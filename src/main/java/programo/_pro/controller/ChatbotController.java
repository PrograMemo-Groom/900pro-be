package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import programo._pro.entity.Chatbot;
import programo._pro.service.ChatbotService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "챗봇 메시지 조회&생성 API")
@RequestMapping("/api/chatbot")
public class ChatbotController {

	private final ChatbotService chatbotService;

	@Operation(summary = "팀 ID로 챗봇 메시지 조회",
			description = "팀 ID에 속한 챗봇 메시지 목록을 조회합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "챗봇 메시지 조회 성공"),
					@ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 : 해당 팀에 속하는 챗봇 메시지를 찾을 수 없습니다."),
					@ApiResponse(responseCode = "500", description = "서버 오류: 메시지 조회 중 문제가 발생했습니다.")
			})
	@GetMapping("/{teamId}")
	public List<Chatbot> getChatbotsByTeamId(@PathVariable Long teamId) {
		return chatbotService.getChatbotsByTeamId(teamId);
	}

	@Operation(summary = "새로운 챗봇 메시지 생성",
			description = "새로운 챗봇 메시지를 생성합니다.",
			responses = {
					@ApiResponse(responseCode = "201", description = "챗봇 메시지 생성 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: 메시지 생성 실패"),
					@ApiResponse(responseCode = "500", description = "서버 오류: 메시지 생성 중 문제가 발생했습니다.")
			})
	@PostMapping
	public void createChatbotMessage(@RequestBody Chatbot chatbot) {
		chatbotService.createChatbotMessage(chatbot);
	}
}
