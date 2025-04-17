package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
}
