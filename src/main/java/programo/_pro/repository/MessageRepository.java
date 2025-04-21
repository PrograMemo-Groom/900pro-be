package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByChatRoom_IdOrderBySendAtAsc(Long chatRoomId);

	// 검색된 메시지가 최신 메시지부터 정렬되도록
	List<Message> findByChatRoom_IdAndContentContainingOrderBySendAtDesc(Long chatRoomId, String keyword);

	List<Message> findByChatRoom_IdAndSendAtBetween(Long chatRoomId, LocalDateTime start, LocalDateTime end);
}