package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import programo._pro.entity.*;
import programo._pro.global.exception.chatException.NotFoundChatException;
import programo._pro.global.exception.teamException.NotFoundTeamException;
import programo._pro.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
	private final ChatbotRepository chatbotRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final SimpMessagingTemplate messagingTemplate;
	TeamMemberRepository teamMemberRepository;

	public void sendMessageToTeam(Long teamId) {
		LocalDateTime now = LocalDateTime.now();
		log.info("[메시지 전송] 현재 시간 : {}", now);

		Team team = teamMemberRepository.findById(teamId)
				.map(TeamMember::getTeam)
				.orElseThrow(NotFoundTeamException::new);

		LocalDateTime testStartTime = team.getStartTime();  // 시험 시작 시간
		log.info("[팀 처리] 팀명: {}, 테스트 시작 시간: {}", team.getTeamName(), testStartTime);

		// 시험 시작 시간 이전인 경우 바로 리턴
		if (now.isBefore(testStartTime)) {
			log.info("[팀 처리] 팀 {}의 테스트가 아직 시작되지 않았습니다.", team.getTeamName());
			return;  // 시험이 시작되지 않았다면 메서드 종료
		}

		log.info("[팀 처리] 팀 {}의 테스트가 시작되었거나 종료되었습니다.", team.getTeamName());

		// 챗봇 메시지 조회
		List<Chatbot> chatbots = chatbotRepository.findByTeam_Id(team.getId());

		// 챗봇 메시지가 없으면 예외 발생
		if (chatbots.isEmpty()) {
			log.warn("[팀 처리] 팀 {}의 챗봇 메시지가 존재하지 않습니다.", team.getTeamName());
			throw NotFoundChatException.NotFoundChatbotException();
		}

		// 챗봇 메시지가 존재하면 생성 및 전송
		for (Chatbot chatbot : chatbots) {
			createAndSendChatbotMessage(chatbot, team);
		}
	}

	// 메시지 생성 및 전송하는 메서드
	private void createAndSendChatbotMessage(Chatbot chatbot, Team team) {
		// 메시지 생성
		chatbot.setSendAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
		chatbot.setTestDate(LocalDate.now().atStartOfDay().atZone(ZoneId.of("Asia/Seoul")));
		chatbot.setMessage("[응시하느라 고생하셨습니다.]");

		// 문제 번호 메시지 추가
		String messageContent = "오늘의 문제 번호: ";
		for (int i = 1; i <= team.getProblemCount(); i++) {
			messageContent += i + " ";
		}
		chatbot.setMessage(messageContent);

		// 메시지 저장
		chatbotRepository.save(chatbot);

		// 메시지 전송
		Long teamId = chatbot.getTeam().getId();
		ChatRoom chatRoom = chatRoomRepository.findByTeam_Id(teamId)
				.orElseThrow(NotFoundChatException::NotFoundChatRoomException);

		messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), messageContent);
		log.info("[메시지 전송] teamId={} 메시지: {}", teamId, chatbot.getMessage());
	}

	public List<Chatbot> getChatbotsByTeamId(Long teamId) {
		return chatbotRepository.findByTeam_Id(teamId);
	}

}