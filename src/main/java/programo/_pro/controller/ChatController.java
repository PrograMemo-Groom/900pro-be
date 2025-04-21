package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.chatDto.ChatMessageRequest;
import programo._pro.dto.chatDto.ChatMessageResponse;
import programo._pro.service.ChatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

	private final ChatService chatService;

	// 채팅방 전체 메시지 조회
	@Operation(
			summary = "채팅방 전체 메시지 조회",
			description = "채팅방의 모든 메시지(사용자 메시지 + 챗봇 메시지)를 시간 순으로 조회합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
					@ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
					@ApiResponse(responseCode = "500", description = "서버 오류")
			})
	@GetMapping("/{chatRoomId}/messages")
	public List<ChatMessageResponse> getAllMessages(
			@Parameter(description = "채팅방 ID", required = true)
			@PathVariable Long chatRoomId) {
		return chatService.getAllMessages(chatRoomId);
	}

	// 날짜별 메시지 조회
	@Operation(
			summary = "날짜별 메시지 조회",
			description = "특정 날짜에 해당하는 메시지를 조회합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
					@ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
			})
	@GetMapping("/{chatRoomId}/messages/date/{date}")
	public List<ChatMessageResponse> getMessagesByDate(
			@Parameter(description = "채팅방 ID", required = true)
			@PathVariable Long chatRoomId,
			@Parameter(description = "조회할 날짜", required = true)
			@PathVariable String date) {
		LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
		LocalDateTime end = LocalDateTime.parse(date + "T23:59:59");
		return chatService.getMessagesByDate(chatRoomId, start, end);
	}

	// 메시지 검색
	@Operation(
			summary = "채팅방 메시지 검색",
			description = "주어진 키워드로 메시지를 검색합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "메시지 검색 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: 키워드가 제공되지 않음"),
					@ApiResponse(responseCode = "500", description = "서버 오류")
			})
	@GetMapping("/{chatRoomId}/messages/search")
	public List<ChatMessageResponse> searchMessages(
			@Parameter(description = "채팅방 ID", required = true)
			@PathVariable Long chatRoomId,
			@Parameter(description = "검색할 키워드", required = true)
			@RequestParam String keyword) {
		return chatService.searchMessagesByKeyword(chatRoomId, keyword);
	}

	// 사용자가 채팅 메시지 전송
	@Operation(
			summary = "사용자가 채팅 메시지 전송",
			description = "사용자가 채팅방에 메시지를 전송하면 이를 처리하는 API입니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: 요청 파라미터 오류"),
					@ApiResponse(responseCode = "500", description = "서버 오류: 메시지 전송 처리 중 문제가 발생했습니다.")
			})
	@PostMapping("/{chatRoomId}/user-send-message")
	public void UserReceiveMessage(
			@Parameter(description = "채팅 메시지 요청 정보")
			@RequestBody ChatMessageRequest messageRequest) {
		chatService.processUserMessage(messageRequest);
	}

	// 챗봇 메시지 전송 - 시험 시작 시간에 맞춰 메시지를 전송
	@Operation(
			summary = "시험 시작 시 챗봇 메시지 전송",
			description = "팀에 대해 시험 시작 시간에 맞춰 챗봇 메시지를 전송합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "챗봇 메시지 전송 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: 메시지 전송 실패"),
					@ApiResponse(responseCode = "500", description = "서버 오류: 메시지 전송 중 문제가 발생했습니다.")
			})
	@PostMapping("/{chatRoomId}/chatbot/test-start")
	public void sendChatbotMessageAtTestStart(
			@Parameter(description = "채팅방 ID에 따라 챗봇 메시지를 전송합니다.", required = true)
			@PathVariable Long chatRoomId) {
		chatService.sendChatbotMessageToTeam(chatRoomId); // 시험 시작 시간에 맞춰 챗봇 메시지 전송
	}
}