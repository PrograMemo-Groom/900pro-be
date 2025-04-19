package programo._pro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import programo._pro.entity.Message;
import programo._pro.repository.MessageRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageRepository messageRepository;

	public List<Message> getMessagesForDate(Long teamId, LocalDate date) {
		// 날짜의 시작 시간을 00:00:00으로 설정
		LocalDateTime startOfDay = date.atStartOfDay();

		// 날짜의 끝 시간을 23:59:59으로 설정
		LocalDateTime endOfDay = date.atTime(23, 59, 59);

		// 주어진 날짜 범위에 해당하는 메시지들 가져오기
		return messageRepository.findByChatRoom_TeamIdAndSendAtBetween(teamId, startOfDay, endOfDay);
	}
}
