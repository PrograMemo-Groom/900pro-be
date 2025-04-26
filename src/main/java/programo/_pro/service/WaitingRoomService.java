package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.waitingRoomDto.ReadyMessageDto;
import programo._pro.entity.*;
import programo._pro.global.exception.testException.TestException;
import programo._pro.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class WaitingRoomService {

    private final TeamMemberRepository teamMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProblemRepository problemRepository;
    private final TestRepository testRepository;
    private final TeamRepository teamRepository;
    private final TestProblemRepository testProblemRepository;
    private final CodeRepository codeRepository;

    // 대기실 처음 입장 시 초기 상태, 팀원 정보
    @Transactional
    public List<ReadyMessageDto> getTeamWaitingStatus(Long teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeam_Id(teamId);

        return teamMembers.stream().map(tm -> new ReadyMessageDto(teamId, tm.getUser().getId(), tm.getUser().getUsername(), "WAITING")).collect(Collectors.toList());
    }

    // 준비 상태 전송 처리 (WebSocket)\
    @Transactional
    public void broadcastReadyStatus(ReadyMessageDto message) {
        String topic = "/sub/waiting-room/" + message.getTeamId();
        messagingTemplate.convertAndSend(topic, message);
        log.info("메시지 보냄: {}", message);
    }

    // Problem 테이블에서 현재 등록되어 있는 문제의 개수로 랜덤 문제를 가져오고 Test, Test_Problem, Problemq 테이블에 저장해야됨
    // 시험 시작 30분 전 문제를 자동으로 생성합니다.
    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행 (테스트 용)
    @Transactional
    public void SetRandomProblem() {
        List<Team> teams = teamRepository.findAll();  // 모든 팀 가져오기

        // 모든 팀 적용
        for (Team team : teams) {
            // 팀의 시험 시작 시간을 가져옴 1500 형식
            String startTime_str = team.getStartTime();

            // formatter 생성 (HHmm 포맷)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            // String -> LocalDate
            LocalTime startTime = LocalTime.parse(startTime_str, formatter);

            // startTime의 30분 전으로 테스트 생성 시간을 변경
            LocalTime generateTime = startTime.minusMinutes(30);

            // 현재 시간을 String으로 포맷팅 합니다
            LocalTime currentTime = LocalTime.now();

            // 현재 시간이 "30분 전" 이후 ~ "30분 후" 사이면 생성 시도
            if (!currentTime.isBefore(generateTime) && currentTime.isBefore(generateTime.plusMinutes(1))) {

                List<Problem> allProblems = problemRepository.findAll();
                int total = allProblems.size();
                int pickCount = team.getProblemCount();

                if (pickCount > total) {
                    throw new IllegalArgumentException("요청한 문제 개수가 전체 문제 수보다 많습니다.");
                }

                // 인덱스를 무작위로 섞고 앞에서부터 pickCount개 가져오기
                Collections.shuffle(allProblems);
                List<Problem> pickedProblems = allProblems.subList(0, pickCount);

                // 문제 리스트 가져오기 완료
//                log.info("📋 랜덤 선택된 문제 리스트: {}", pickedProblems);


                // 테스트 테이블 객체 초기화
                Test test = new Test();
                test.setTeam(team);
                test.setCreatedAt(LocalDate.now());

                // 테스트 테이블 삽입 완료
                Test savedTest = testRepository.save(test);

                // TestProblem에 넣을 test_id
                long savedTestId = savedTest.getId();

                // 뽑은 문제들의 PK를 뽑아서 저장할 리스트
                List<Integer> problemIdList = new ArrayList<>();

                // 각 문제의 PK를 뽑아서 리스트에 추가
                pickedProblems.forEach(p -> {
                    problemIdList.add(pickedProblems.indexOf(p));
                });

                // 팀의 문제 개수 개수만큼 반복, 각 문제마다 test_problem 테이블 저장
                for (int i = 0; i < pickCount; i++) {
                    TestProblem testProblem = new TestProblem();
                    testProblem.setTest(savedTest);
                    testProblem.setProblem(pickedProblems.get(i));
                    testProblemRepository.save(testProblem);
                }

                log.info("테스트가 정상적으로 생성되었습니다. testId : {} ", savedTestId);
            } else {
                log.info("teamName : {} 아직 시험 30분 전이 되지 않았습니다. 문제 생성을 하지 않습니다.", team.getTeamName());
            }
        }
    }

    // teamId를 입력 받고 테스트 아이디를 가져와서 해당 테스트의 모든 유저들의 상태를 ABSENT 초기화
    public void initUser(Long teamId) {
        // teamId 로 테스트 리스트 조회
        List<Test> tests = testRepository.findByTeam_Id(teamId);

        if (tests.isEmpty()) {
            log.warn("해당 팀에 등록된 테스트가 없습니다. teamId={}", teamId);
            throw TestException.NotFoundTestException();
        }

        Test latestTest = tests.get(tests.size() - 1); // 리스트 마지막 요소
        long testId = latestTest.getId();

        // 테스트의 문제들을 조회
        List<TestProblem> problems = testProblemRepository.findAllByTest_id(testId);


        // 해당 팀의 멤버들을 조회
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeam_Id(teamId);

        // 팀 멤버를 모두 순회
        teamMembers.forEach(tm -> {
            // 테스트의 문제들을 순회
            problems.forEach(p -> {
                // 각 문제에 대해 코드 엔티티를 생성
                Code newCode = new Code();
                newCode.setTest(latestTest);
                newCode.setProblem(p.getProblem());
                newCode.setStatus(Status.ABSENT);
                newCode.setSubmitCode("");
                newCode.setUser(tm.getUser());
                newCode.setLanguage("Python");

                codeRepository.save(newCode);
            });
        });
    }
}
