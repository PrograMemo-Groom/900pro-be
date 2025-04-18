package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByChatRoom_Id(Long chatRoomId);
	List<Message> findByChatRoom_IdOrderBySendAtAsc(Long chatRoomId);
}