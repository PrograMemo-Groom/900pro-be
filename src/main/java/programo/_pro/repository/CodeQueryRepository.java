package programo._pro.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import programo._pro.entity.Code;
import programo._pro.entity.CodeHighight;
import programo._pro.entity.QCode;
import programo._pro.entity.QCodeHighight;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CodeQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<Code> findCodeByTestIdAndProblemIdAndUserId(int testId, int problemId, int userId) {
        QCode qCode = QCode.code;
//        QCodeHighight qHighlight = QCodeHighight.codeHighight;

        return jpaQueryFactory
                .selectFrom(qCode)
//                .leftJoin(qCode.codeHighlight, qHighlight).fetchJoin() // ✅ 연관관계 명확히 하고 fetchJoin
                .where(
                        qCode.test.id.eq((long) testId),
                        qCode.problem.id.eq((long) problemId),
                        qCode.user.id.eq((long) userId)
                )
                .fetch();
    }

    public List<CodeHighight> findHighlightByCodeIdAndUserId(int codeId, int userId) {
        QCodeHighight qHighlight = QCodeHighight.codeHighight;

        return jpaQueryFactory
                .selectFrom(qHighlight)
                .where(
                        qHighlight.user.id.eq((long) userId),
                        qHighlight.code.id.eq((long) codeId)
                )
                .fetch();

    }
}
