package programo._pro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import programo._pro.dto.ChatMessageRequest;
import programo._pro.entity.ChatRoom;
import programo._pro.entity.Message;
import programo._pro.entity.User;
import programo._pro.global.exception.NotFoundChatException;
import programo._pro.repository.ChatRoomRepository;
import programo._pro.repository.MessageRepository;
import programo._pro.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional
	public void handleChatMessage(ChatMessageRequest request){
		log.info("[채팅 메시지 수신] ChatRoomId={}, UserId={}, Content={}",
				request.getChatRoomId(), request.getUserId(), request.getContent());

		ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
				.orElseThrow(NotFoundChatException::NotFoundChatRoomException);

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(NotFoundChatException::NotFoundUserException);

		Message message = Message.builder()
				.chatRoom(chatRoom)
				.user(user)
				.content(request.getContent())
				.sendAt(LocalDateTime.now())
				.build();

		messageRepository.save(message);

		log.info("[메시지 저장 완료] messageId={}, sendTo=/sub/chat/room/{}",
				message.getId(), request.getChatRoomId());

		messagingTemplate.convertAndSend("/sub/chat/room/" + request.getChatRoomId(), request);

		log.info("[메시지 전송 완료] 대상 채널: /sub/chat/room/{}", request.getChatRoomId());
	}
}