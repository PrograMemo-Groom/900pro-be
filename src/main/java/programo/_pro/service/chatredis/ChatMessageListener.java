package programo._pro.service.chatredis;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatMessageListener {

	private final SimpMessagingTemplate messagingTemplate;

	// Redis에서 메시지가 도착했을 때 호출되는 메서드
	public void onMessage(String message) {
		// 받은 메시지를 WebSocket으로 전송
		String chatRoomId = "chatroom";  // 채팅방 ID는 적절히 변경
		messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoomId, message);

		// 로그로 확인
		System.out.println("Received message from Redis: " + message);
	}
}