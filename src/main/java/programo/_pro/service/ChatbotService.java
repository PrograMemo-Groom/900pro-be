package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import programo._pro.entity.ChatRoom;
import programo._pro.entity.Chatbot;
import programo._pro.entity.Team;
import programo._pro.repository.ChatRoomRepository;
import programo._pro.repository.ChatbotRepository;
import programo._pro.repository.TeamRepository;

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
	private final TeamRepository teamRepository;

	@Scheduled(cron = "0 0 0 * * *")
	public void sendScheduledMessages() {
		LocalDateTime now = LocalDateTime.now();
		log.info("[스케줄러 실행] 현재 시간 : {}", now);

		List<Team> teams = teamRepository.findAll();

		for (Team team : teams) {
			LocalDateTime testEndTime = calculateTestEndTime(team.getStartTime(), team.getDurationTime());
			log.info("[팀 처리] 팀명: {}, 테스트 종료 시간: {}", team.getTeamName(), testEndTime);

			if (now.isAfter(testEndTime)) {
				log.info("[팀 처리] 팀 {}의 테스트가 종료되었습니다.", team.getTeamName());

				List<Chatbot> chatbots = chatbotRepository.findByTeam_Id(team.getId());

				for (Chatbot chatbot : chatbots) {
					sendMessageToChatRoom(chatbot, team);
				}
			} else{
				log.info("[팀 처리] 팀 {}의 테스트가 아직 종료되지 않았습니다.", team.getTeamName());
			}
		}
	}

	private void sendMessageToChatRoom(Chatbot chatbot, Team team) {
		Long teamId = chatbot.getTeam().getId();
		log.info("[메시지 전송] teamId={} 메시지: {}", teamId, chatbot.getMessage());

		ChatRoom chatRoom = chatRoomRepository.findByTeamId(teamId)
				.orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

		String messageContent = "[" + LocalDate.now() + "]\n";
		messageContent += "응시하느라 고생하셨습니다!\n";
		messageContent += "오늘의 문제 번호 : ";
		for (int i = 1; i <= team.getProblemCount(); i++) {
			messageContent += i + " ";
		}

		messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), messageContent);
		log.info("[Chatbot 메시지 전송] teamId={} 메시지={}", chatRoom.getId(), chatbot.getMessage());
	}

	public LocalDateTime calculateTestEndTime(LocalDateTime startTime, int durationTimeInMinutes) {
		log.info("[종료 시간 계산] 시작 시간: {}, durationTimeInMinutes: {}", startTime, durationTimeInMinutes);
		LocalDateTime endTime = startTime.plusMinutes(durationTimeInMinutes);
		log.info("[종료 시간 계산 완료] 종료 시간: {}", endTime);
		return endTime;
	}
}