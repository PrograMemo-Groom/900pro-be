package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.codeDto.CodeRequestDto;
import programo._pro.dto.codeDto.CodeResponseDto;
import programo._pro.dto.codeDto.UpdateCodeDto;
import programo._pro.dto.highlightDto.CodeHighlightResponseDto;
import programo._pro.entity.*;
import programo._pro.global.exception.codeException.CodeException;
import programo._pro.global.exception.userException.UserException;
import programo._pro.repository.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeService {
    private final CodeRepository codeRepository;
    private final CodeQueryRepository codeQueryRepository;
    private final CodeHighlightRepository codeHighlightRepository;
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final TestProblemQueryRepository problemQueryRepository;


    // 팀원의 첫번째 풀이와 하이라이트 정보를 조회
    @Transactional
    public Map<String, Object> getCodeMemberCodeAndHighlight(int testId, int problemId, int userId) {
        // 풀이와 하이라이트 정보를 함께 담을 데이터 포맷
        Map<String, Object> data = new HashMap<>();

        // 하이라이트 리스트
        List<CodeHighlightResponseDto> highlightResponseDtos = new ArrayList<>();


        // 팀원의 풀이를 가져오기
        List<Code> codeList = codeQueryRepository.findCodeByTestIdAndProblemIdAndUserId(testId, problemId, userId);

        // 코드 정보와 하이라이트 테이블이 담긴 정보 1개 조회
        Code first = codeList.stream().findFirst().orElseThrow(CodeException::NotFoundCodeException);

        // 제출한 풀이 응답객체 생성
        CodeResponseDto codeResponseDto = CodeResponseDto.builder()
                .codeId(first.getId())
                .submitCode(first.getSubmitCode())
                .language(first.getLanguage())
                .status(first.getStatus())
                .submitAt(first.getSubmitAt())
                .build();


        // 해당 풀이의 하이라이트 리스트 조회
        codeQueryRepository.findHighlightByCodeIdAndUserId((int) first.getId(), userId).forEach(
                highlight -> {
                    CodeHighlightResponseDto codeHighlightResponseDto = CodeHighlightResponseDto.builder()
                            .highlightId(highlight.getId())
                            .startPosition(highlight.getStartPosition())
                            .endPosition(highlight.getEndPosition())
                            .color(highlight.getColor())
                            .memo(highlight.getMemo())
                            .isActive(highlight.isActive())
                            .build();

                    // 매 순회마다 리스트에 추가
                    highlightResponseDtos.add(codeHighlightResponseDto);
                }
        );


        // Json 추가
        data.put("code", codeResponseDto);
        data.put("highlights", highlightResponseDtos);
        // 하이라이트 id 응답
//        data.put("highlightId", first.getId());

        return data;
    }

    // user_id, test_id를 입력받아 해당 유저의 문제풀이들을 시험완료 상태로 업데이트
    @Transactional
    public void updateSubmitCode(CodeRequestDto codeRequestDto) {
        int testId = codeRequestDto.getTestId();
        int userId = codeRequestDto.getUserId();

        // 유저의 코딩 중 여부 업데이트
        User user = userRepository.findById((long) userId).orElseThrow(UserException::byId);
        user.setCoding(false);
        userRepository.save(user);

        // testId, userId가 일치하는 문제들을 조회합니다
        List<Code> userCodes = codeRepository.findByTest_IdAndUser_Id(testId, userId);

        userCodes.forEach(userCode -> {
            userCode.setStatus(Status.COMPLETED);
            codeRepository.save(userCode);
        });
    }


    // 작성한 코드 DB 업데이트 기능
    @Transactional
    public void updateCode(UpdateCodeDto updateCodeDto) {
        int testId = updateCodeDto.getCodeRequestDto().getTestId();
        int userId = updateCodeDto.getCodeRequestDto().getUserId();
        int problemId = updateCodeDto.getCodeRequestDto().getProblemId();

        // 해당 제출 코드를 가져옴
        Code findUserCode = codeRepository.findByTest_IdAndUser_IdAndProblem_Id(testId, userId, problemId);

        // updateCodeDto에 포함된 작성 코드로 업데이트
        findUserCode.setSubmitCode(updateCodeDto.getSubmitCode());
        findUserCode.setSubmitAt(updateCodeDto.getSubmitAt());

        // 업데이트
        codeRepository.save(findUserCode);
    }



    // 테스트의 문제 리스트 조회
    @Transactional(readOnly = true)
    public List<Problem> getProblemsByTestId(int testId) {
        List<Problem> problems = problemQueryRepository.findProblemsByTestId(testId);

        return problems;
    }
}
