package programo._pro.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.highlightDto.CodeHighlightResponseDto;
import programo._pro.entity.CodeHighight;
import programo._pro.entity.Problem;
import programo._pro.global.exception.problemException.ProblemException;
import programo._pro.repository.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class HistoryService {

    private final TestRepository testRepository;
    private final TeamRepository teamRepository;
    private final TestProblemRepository testProblemRepository;
    private final TestProblemQueryRepository problemQueryRepository;
    private final ProblemRepository problemRepository;
    private final CodeHighlightRepository codeHighlightRepository;


    // 해당 test_id의 모든 문제 정보들을 조회합니다
    @Transactional(readOnly = true)
    public List<Problem> getHistory(int testId) {
        Map<String, Object> data = new HashMap<>();

        // 문제 정보 리스트
//        List<Map<String, Object>> problemList = new java.util.ArrayList<>();

        List<Problem> problems = problemQueryRepository.findProblemsByTestId(testId);

        // 테스트 id로 조회한 문제들 리스트
        log.info("problem List : {} ", problems.toString());

        // 첫 번째 문제 정보 가져오기 (옵션)
//        Problem firstProblem = problems.stream().findFirst()
//                .orElseThrow(ProblemException::NotFoundProblemException);


//        // 반복하며 각각의 문제 정보 추가
//        for (Problem p : problems) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("problem_id", p.getId());
//            map.put("problem_baek_num", p.getBaekNum());
//            problemList.add(map);
//        }

        // ✅ 최종 결과 구조
//        data.put("problemList", problemList); // 여러 문제 정보
//        data.put("problem", firstProblem);        // 첫 번째 문제 정보

        return problems;
    }

    // 문제 번호로 문제 정보 조회
//    public Problem getProblem(int problemId) {
//
//        return problemRepository.findById(problemId).orElseThrow(ProblemException::NotFoundProblemException);
//    }
}
