package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.problemDto.ProblemGenerateRequestDto;
import programo._pro.dto.waitingRoomDto.ReadyMessageDto;
import programo._pro.entity.*;
import programo._pro.global.exception.teamException.TeamException;
import programo._pro.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    // 대기실 처음 입장 시 초기 상태, 팀원 정보
    @Transactional
    public List<ReadyMessageDto> getTeamWaitingStatus(Long teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeam_Id(teamId);

        return teamMembers.stream()
                .map(tm -> new ReadyMessageDto(
                        teamId,
                        tm.getUser().getId(),
                        tm.getUser().getUsername(),
                        "WAITING"
                ))
                .collect(Collectors.toList());
    }

    // 준비 상태 전송 처리 (WebSocket)\
    @Transactional
    public void broadcastReadyStatus(ReadyMessageDto message) {
        String topic = "/sub/waiting-room/" + message.getTeamId();
        messagingTemplate.convertAndSend(topic, message);
        log.info("메시지 보냄: {}", message);
    }

    // Problem 테이블에서 현재 등록되어 있는 문제의 개수로 랜덤 문제를 가져오고 Test, Test_Problem, Problemq 테이블에 저장해야됨
    @Transactional
    public Map<String, Object> SetRandomProblem(ProblemGenerateRequestDto requestDto) {
        Map<String, Object> data = new HashMap<>();

        List<Problem> allProblems = problemRepository.findAll();
        int total = allProblems.size();
        int pickCount = requestDto.getProblemCount();

        if (pickCount > total) {
            throw new IllegalArgumentException("요청한 문제 개수가 전체 문제 수보다 많습니다.");
        }

        // 인덱스를 무작위로 섞고 앞에서부터 pickCount개 가져오기
        Collections.shuffle(allProblems);
        List<Problem> pickedProblems = allProblems.subList(0, pickCount);

        // 문제 리스트 가져오기 완료
        log.info("📋 랜덤 선택된 문제 리스트: {}", pickedProblems);

        Team team = teamRepository.findById(requestDto.getTeamId()).orElseThrow(TeamException::NotFoundTeamException);

        // 테스트 테이블 객체 초기화
        Test test = new Test();
        test.setTeam(team);
        test.setCreatedAt(LocalDateTime.now());

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
        for(int i = 0; i < pickCount; i++) {
            TestProblem testProblem = new TestProblem();
            testProblem.setTest(savedTest);
            testProblem.setProblem(pickedProblems.get(i));
            testProblemRepository.save(testProblem);
        }

        data.put("testId", savedTestId);

        return data;
    }
}
