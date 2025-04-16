package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Chatbot;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatbotRepository extends JpaRepository<Chatbot, Long> {

	List<Chatbot> findAllBySendAtBefore(LocalDateTime currentTime);

	List<Chatbot> findByTeam_Id(Long TeamId);
}
