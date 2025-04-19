package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.PasswordToken;

public interface PasswordTokenRepository extends JpaRepository<PasswordToken, Long> {

}
