package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Integer> {
}
