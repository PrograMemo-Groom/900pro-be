package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Code;

import java.util.List;

public interface CodeRepository extends JpaRepository<Code, Long> {
    List<Code> findByTest_IdAndUser_Id(long testId, long userId);
}
