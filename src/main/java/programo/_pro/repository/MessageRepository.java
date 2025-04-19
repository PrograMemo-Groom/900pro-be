package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByChatRoom_Id(Long chatRoomId);
	List<Message> findByChatRoom_IdOrderBySendAtAsc(Long chatRoomId);

	// 특정 날짜 범위에서 메시지를 조회
	List<Message> findByChatRoom_TeamIdAndSendAtBetween(Long teamId, LocalDateTime start, LocalDateTime end);
}