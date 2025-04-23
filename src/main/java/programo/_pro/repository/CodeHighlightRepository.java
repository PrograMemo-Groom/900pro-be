package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.CodeHighight;

import java.util.List;

public interface CodeHighlightRepository extends JpaRepository<CodeHighight, Long> {
//    List<CodeHighight> findByProblemId(long firstProblemId);
}
