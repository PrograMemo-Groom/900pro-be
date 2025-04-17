package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import programo._pro.entity.ChatRoom;
import programo._pro.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ChatRoom createChatRoom(@RequestBody Long teamId) {
		return chatRoomService.createChatRoom(teamId);
	}

	@GetMapping("/{teamId}")
	public ChatRoom getChatRoom(@PathVariable Long teamId) {
		return chatRoomService.getChatRoomByTeamId(teamId);
	}
}
