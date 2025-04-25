package programo._pro.dto.chatDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageResponse {
	private Long id;              // 메시지 ID
	private Long chatRoomId;      // 채팅방 ID
	private Long userId;          // 사용자 ID (챗봇 메시지의 경우 null)
	private String userName;      // 사용자 이름 (챗봇 메시지의 경우 "Chatbot" 등 고정값)
	private String content;       // 메시지 내용
	private LocalDateTime sendAt; // 메시지 전송 시간
	private boolean isChatbot;   // 챗봇 여부
	private LocalDateTime testDateTime;  // 챗봇 메시지에만 해당 (시험 날짜)
	private String chatbotMessage;  // 챗봇 메시지 내용

	public ChatMessageResponse(Long id, Long chatRoomId, Long userId, String userName, String content, LocalDateTime sendAt, boolean isChatbot, LocalDateTime testDateTime, String chatbotMessage) {
		this.id = id;
		this.chatRoomId = chatRoomId;
		this.userId = userId;
		this.userName = userName;
		this.content = content;
		this.sendAt = sendAt;
		this.isChatbot = isChatbot;
		this.testDateTime = testDateTime;
		this.chatbotMessage = chatbotMessage;
	}
}