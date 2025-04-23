package programo._pro.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import programo._pro.entity.Problem;
import programo._pro.entity.QProblem;
import programo._pro.entity.QTestProblem;

import java.util.List;


@RequiredArgsConstructor
@Repository
public class TestProblemQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<Problem> findProblemsByTestId(long testId) {
        QTestProblem testProblem = QTestProblem.testProblem;
        QProblem problem = QProblem.problem;

        return jpaQueryFactory
                .select(testProblem.problem)
                .from(testProblem)
                .join(testProblem.problem, problem)
                .where(testProblem.test.id.eq(testId))
                .fetch();
    }
}
