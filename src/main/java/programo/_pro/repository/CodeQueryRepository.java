package programo._pro.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import programo._pro.entity.Code;
import programo._pro.entity.QCode;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CodeQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;


    // test_id와 problem_id로 해당 problem_id 의 유저들의 풀이 리스트를 조회
    public List<Code> findCodeByTestIdAndProblemId(long testId, long problemId) {
        QCode qCode = QCode.code;


        List<Code> codes = jpaQueryFactory
                .selectFrom(qCode) // Code 테이블에서 모든 컬럼을 가져온다
                .where(qCode.id.eq(testId) // Code 테이블의 test_id 가 일치할 때
                        .and(qCode.problem.id.eq(problemId))) // Code 테이블의 problem_id가 일치할 때
                .fetch();

        return codes;
    }

}
