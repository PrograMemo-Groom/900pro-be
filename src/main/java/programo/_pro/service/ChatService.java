package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import programo._pro.dto.chatDto.ChatMessageRequest;
import programo._pro.dto.chatDto.ChatMessageResponse;
import programo._pro.entity.*;
import programo._pro.global.exception.chatException.NotFoundChatException;
import programo._pro.global.exception.teamException.NotFoundTeamException;

import programo._pro.repository.*;
import programo._pro.service.chatredis.ChatPublisherService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;
	private final ChatbotRepository chatbotRepository;
	private final TeamRepository teamRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatPublisherService chatPublisherService;

	// 팀이 생성되면 자동으로 채팅방 생성
	public void createChatRoom(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(NotFoundTeamException::new);

		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setTeam(team);
		chatRoomRepository.save(chatRoom);
	}

	public List<ChatMessageResponse> getAllMessages(Long chatRoomId) {
		// 사용자 메시지 조회
		List<Message> messages = messageRepository.findByChatRoom_IdOrderBySendAtAsc(chatRoomId);
		// 챗봇 메시지 조회
		List<Chatbot> chatbots = chatbotRepository.findByTeam_Id(chatRoomId);

		// 사용자 메시지 + 챗봇 메시지 가져오기
		List<ChatMessageResponse> chatMessages = messages.stream()
				.map(message -> new ChatMessageResponse(
						message.getId(),
						message.getChatRoom().getId(),
						message.getUser().getId(),
						message.getUser().getUsername(),
						message.getContent(),
						message.getSendAt().toLocalDateTime(),
						false,      // 사용자 메시지는 isChatbot = false
						null,       // 챗봇 관련 정보는 null
						null))      // 챗봇 관련 메시지 내용은 null
				.collect(Collectors.toList());

		// 챗봇 메시지 추가
		chatbots.forEach(chatbot -> {
			chatMessages.add(new ChatMessageResponse(
					chatbot.getId(),
					chatbot.getTeamId(),
					null,  // 챗봇에는 사용자 ID가 없으므로 null
					"Chatbot",  // 고정값: 챗봇 메시지
					chatbot.getMessage(),
					chatbot.getSendAt().toLocalDateTime(),
					true,       // 챗봇 메시지는 isChatbot = true
					chatbot.getTestDate().toLocalDateTime(),  // 챗봇 메시지의 시험 날짜
					chatbot.getMessage())); // 챗봇 메시지 내용
		});

		// 메시지 시간순으로 정렬
		chatMessages.sort((msg1, msg2) -> {
			if (msg1.getSendAt() == null && msg2.getSendAt() == null) {
				return 0;
			} else if (msg1.getSendAt() == null) {
				return 1;
			} else if (msg2.getSendAt() == null) {
				return -1;
			}
			return msg1.getSendAt().compareTo(msg2.getSendAt());
		});

		return chatMessages;
	}


	// 날짜별 메시지 조회
	public List<ChatMessageResponse> getMessagesByDate(Long chatRoomId, LocalDateTime start, LocalDateTime end) {
		List<Message> messages = messageRepository.findByChatRoom_IdAndSendAtBetween(chatRoomId, start, end);
		return messages.stream()
				.map(message -> new ChatMessageResponse(
						message.getId(),
						message.getChatRoom().getId(),
						message.getUser().getId(),
						message.getUser().getUsername(),
						message.getContent(),
						message.getSendAt().toLocalDateTime(),
						false,      // 사용자 메시지는 isChatbot = false
						null,       // 챗봇 관련 정보는 null
						null))      // 챗봇 관련 메시지 내용은 null
				.collect(Collectors.toList());
	}

	// 키워드로 메시지 검색
	public List<ChatMessageResponse> searchMessagesByKeyword(Long chatRoomId, String keyword) {
		// 검색된 메시지를 최신순으로 가져오기
		List<Message> messages = messageRepository.findByChatRoom_IdAndContentContainingOrderBySendAtDesc(chatRoomId, keyword);
		return messages.stream()
				.map(message -> {
					// 키워드 강조
					String highlightedContent = message.getContent().replaceAll("(?i)" + keyword, "<span style='color:yellow;'>" + keyword + "</span>");
					return new ChatMessageResponse(
							message.getId(),
							message.getChatRoom().getId(),
							message.getUser().getId(),
							message.getUser().getUsername(),
							highlightedContent,
							message.getSendAt().toLocalDateTime(),
							false,
							null,
							null
					);
				})
				.collect(Collectors.toList());
	}

	// 사용자가 채팅 메시지 전송
	public void processUserMessage(ChatMessageRequest request) {
		ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
				.orElseThrow(NotFoundChatException::NotFoundChatRoomException);

		// 메시지 생성 및 저장
		Message message = new Message();
		message.setChatRoom(chatRoom);
		message.setUser(request.getUser());
		message.setContent(request.getContent());
		message.setSendAt(ZonedDateTime.now());

		messageRepository.save(message);

		// Redis에 채팅 메시지 발행
		chatPublisherService.publishMessage(chatRoom.getId().toString(), request.getContent());

		// 메시지 전송
		ChatMessageResponse response = new ChatMessageResponse(
				message.getId(),
				chatRoom.getId(),
				message.getUser().getId(),
				message.getUser().getUsername(),
				message.getContent(),
				message.getSendAt().toLocalDateTime(),
				false,
				null,
				null
		);

		messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), response);
	}

	@Scheduled(cron = "0 0/5 * * * ?")  // 매일 5분마다 실행
	public void scheduleChatbotMessage() {
		List<Team> teams = teamRepository.findAll();  // 모든 팀 가져오기
		LocalDateTime now = LocalDateTime.now();  // 현재 시간

		for (Team team : teams) {
			LocalDateTime testStartTime = team.getStartTime(); // 각 팀의 시험 시작 시간

			// 팀의 시험 시작 시간이 오늘과 동일한 경우, 해당 팀에 챗봇 메시지 전송
			if (testStartTime.toLocalDate().isEqual(now.toLocalDate())) {
				log.info("[팀 처리] 팀 {}의 시험 시작 시간이 도래했습니다. 챗봇 메시지 전송 시작.", team.getTeamName());
				sendChatbotMessageToTeam(team.getId());  // 해당 팀에 챗봇 메시지 전송
			}
		}
	}

	// 챗봇 메시지 전송 (팀에 맞게 챗봇 메시지를 전송)
	public void sendChatbotMessageToTeam(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(NotFoundTeamException::new);

		// 시험 시작 시간 체크 (만약 시험 시작 시간이 지나지 않았다면 메시지 전송하지 않음)
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime testStartTime = team.getStartTime();
		if (now.isBefore(testStartTime)) {
			log.info("[팀 처리] 팀 {}의 시험 시작 시간이 아직 되지 않았습니다.", team.getTeamName());
			return;  // 시험이 시작되지 않았다면 메서드 종료
		}

		log.info("[팀 처리] 팀 {}의 시험이 시작되었거나 종료되었습니다.", team.getTeamName());

		// 챗봇 메시지 조회
		List<Chatbot> chatbots = chatbotRepository.findByTeam_Id(team.getId());

		if (chatbots.isEmpty()) {
			throw NotFoundChatException.NotFoundChatbotException();
		}

		// 챗봇 메시지 필터링: 해당 날짜에 시험 시작 시간일 경우, 메시지만 전송
		chatbots.stream()
				.filter(chatbot -> chatbot.getTestDate().toLocalDate().isEqual(now.toLocalDate()))  // 현재 날짜와 시험 날짜가 일치하는 메시지만 전송
				.forEach(chatbot -> createAndSendChatbotMessage(chatbot, team));  // 해당 날짜의 챗봇 메시지만 보내기
	}

	// 공통적인 챗봇 메시지 생성 및 전송 형식
	private void createAndSendChatbotMessage(Chatbot chatbot, Team team) {
		// 메시지 생성
		chatbot.setSendAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
		chatbot.setTestDate(LocalDate.now().atStartOfDay().atZone(ZoneId.of("Asia/Seoul")));
		chatbot.setMessage("응시하느라 고생하셨습니다.");

		// 문제 번호 메시지 추가
		StringBuilder messageContent = new StringBuilder("오늘의 문제 번호: ");
		for (int i = 1; i <= team.getProblemCount(); i++) {
			messageContent.append(i).append(" ");
		}
		chatbot.setMessage(messageContent.toString());

		// 메시지 저장
		chatbotRepository.save(chatbot);

		// 메시지 전송
		Long teamId = chatbot.getTeam().getId();
		ChatRoom chatRoom = chatRoomRepository.findByTeam_Id(teamId)
				.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

		messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), messageContent.toString());
		log.info("[메시지 전송] teamId={} 메시지: {}", teamId, chatbot.getMessage());
	}
}
