package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Code;
import programo._pro.entity.Problem;
import programo._pro.entity.Test;

import java.util.List;

public interface CodeRepository extends JpaRepository<Code, Long> {
    List<Code> findByTest_IdAndUser_Id(long testId, long userId);

    Code findByTest_IdAndUser_IdAndProblem_Id(long testId, long userId, long problemId);

    long test(Test test);

    long problem(Problem problem);
}
