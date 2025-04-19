package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import programo._pro.dto.ChatbotRequest;
import programo._pro.entity.*;
import programo._pro.global.exception.NotFoundChatException;
import programo._pro.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
	private final ChatbotRepository chatbotRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final SimpMessagingTemplate messagingTemplate;
	TeamMemberRepository teamMemberRepository;

	@Scheduled(cron = "0 0 0 * * *")
	public void sendScheduledMessages() {
		LocalDateTime now = LocalDateTime.now();
		log.info("[스케줄러 실행] 현재 시간 : {}", now);

		Long userId = getCurrentUserId();
		List<TeamMember> teamMembers = teamMemberRepository.findByUserId(userId);

		for (TeamMember teamMember : teamMembers) {
			Team team = teamMember.getTeam();
			LocalDateTime testStartTime = team.getStartTime();
			log.info("[팀 처리] 팀명: {}, 테스트 시작 시간: {}", team.getTeamName(), testStartTime);

			if (now.isEqual(testStartTime) || now.isAfter(testStartTime)) {
				log.info("[팀 처리] 팀 {}의 테스트가 종료되었습니다.", team.getTeamName());

				List<Chatbot> chatbots = chatbotRepository.findByTeam_Id(team.getId());

				if (!chatbots.isEmpty()) {
					for (Chatbot chatbot : chatbots) {
						sendMessageToChatRoom(chatbot, team);
					}
				} else {
					log.warn("[팀 처리] 팀 {}의 챗봇 메시지가 존재하지 않습니다.", team.getTeamName());
					throw NotFoundChatException.NotFoundChatbotException();
				}
			} else {
				log.info("[팀 처리] 팀 {}의 테스트가 아직 시작되지 않았습니다.", team.getTeamName());
			}
		}
	}

	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return Long.parseLong(authentication.getName());
	}

	private void sendMessageToChatRoom(Chatbot chatbot, Team team) {
		Long teamId = chatbot.getTeam().getId();
		log.info("[메시지 전송] teamId={} 메시지: {}", teamId, chatbot.getMessage());

		ChatRoom chatRoom = chatRoomRepository.findByTeam_Id(teamId)
				.orElseThrow(NotFoundChatException::NotFoundChatRoomException);

		String messageContent = "[" + LocalDate.now() + "]\n";
		messageContent += "응시하느라 고생하셨습니다!\n";
		messageContent += "오늘의 문제 번호 : ";
		for (int i = 1; i <= team.getProblemCount(); i++) {
			messageContent += i + " ";
		}

		messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), messageContent);
		log.info("[Chatbot 메시지 전송] teamId={} 메시지={}", chatRoom.getId(), chatbot.getMessage());
	}

	public List<Chatbot> getChatbotsByTeamId(Long teamId) {
		return chatbotRepository.findByTeam_Id(teamId);
	}

	public void createChatbotMessage(ChatbotRequest chatbotRequest) {
		Chatbot chatbot = new Chatbot();
		chatbot.setTeamId(chatbotRequest.getTeamId());
		chatbot.setTestDate(chatbotRequest.getTestDate().atStartOfDay());
		chatbot.setMessage(chatbotRequest.getMessage());
		chatbot.setSendAt(LocalDateTime.now());

		String messageContent = chatbotRequest.getMessage() + "\n오늘의 문제 번호: ";
		for (Integer problemNumber : chatbotRequest.getProblemNumbers()) {
			messageContent += problemNumber + " ";
		}
		chatbot.setMessage(messageContent);

		chatbotRepository.save(chatbot);
	}
}