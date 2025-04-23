package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Code;

public interface CodeRepository extends JpaRepository<Code, Long> {
}
