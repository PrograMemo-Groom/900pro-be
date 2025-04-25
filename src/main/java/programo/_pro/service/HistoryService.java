package programo._pro.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.entity.Problem;
import programo._pro.entity.Test;
import programo._pro.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class HistoryService {

    private final TestRepository testRepository;
    private final TestProblemQueryRepository problemQueryRepository;

    // 해당 test_id의 모든 문제 정보들을 조회합니다
    @Transactional(readOnly = true)
    public List<Problem> getHistory(int teamId, LocalDate date) {

        // 해당 팀의 해당 날짜의 테스트를 불러옴
        List<Test> tests = testRepository.findByTeamIdAndCreatedAt((long) teamId, date);

        // 해당 날짜의 테스트 1개 가져옴
        Long testId = tests.stream()
                .findFirst()
                .map(Test::getId)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 테스트가 없습니다."));

        // 테스트의 아이디 확인
        log.info("testId {}", testId);

        // 테스트 아이디로 문제 리스트 조회
        List<Problem> problems = problemQueryRepository.findProblemsByTestId(testId);

        // 테스트 id로 조회한 문제들 리스트
        log.info("problem List : {} ", problems.toString());

        return problems;
    }
}
