package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.TestProblem;

import java.util.List;

public interface TestProblemRepository extends JpaRepository<TestProblem, Integer> {

    List<TestProblem> findAllByTest_id(long testId);
}
