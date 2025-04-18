package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.ChatRoomRequest;
import programo._pro.entity.ChatRoom;
import programo._pro.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chatroom", description = "채팅방 조회&생성 API")
@RequestMapping("/api/chatroom")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@Operation(summary = "채팅방 생성",
			description = "팀 생성과 함께, 새로운 채팅방을 생성합니다.",
			responses = {
					@ApiResponse(responseCode = "201", description = "채팅방 생성 성공"),
					@ApiResponse(responseCode = "400", description = "잘못된 요청: 팀 ID가 존재하지 않거나 유효하지 않습니다."),
					@ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
			})
	@PostMapping
	public ChatRoom createChatRoom(
			@Parameter(description = "새로 생성할 팀 ID")
			@RequestBody ChatRoomRequest chatRoomRequest) {
		return chatRoomService.createChatRoom(chatRoomRequest.getTeamId());
	}

	@Operation(summary = "팀 ID로 채팅방 조회",
			description = "특정 팀 ID에 해당하는 채팅방을 조회합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "채팅방 조회 성공"),
					@ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
			})

	@GetMapping("/{teamId}")
	public ChatRoom getChatRoomByTeamId(
			@Parameter(description = "조회할 팀의 ID")
			@PathVariable Long teamId) {
		return chatRoomService.getChatRoomByTeamId(teamId);
	}
}
