package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import programo._pro.dto.codeDto.CodeResponseDto;
import programo._pro.dto.highlightDto.CodeHighlightResponseDto;
import programo._pro.entity.Code;
import programo._pro.global.exception.codeException.CodeException;
import programo._pro.repository.CodeHighlightRepository;
import programo._pro.repository.CodeQueryRepository;
import programo._pro.repository.CodeRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeService {
    private final CodeRepository codeRepository;
    private final CodeQueryRepository codeQueryRepository;
    private final CodeHighlightRepository codeHighlightRepository;


    // 팀원의 첫번째 풀이와 하이라이트 정보를 조회
    public Map<String, Object> getCodeMemberCodeAndHighlight(int test_id, int problem_id, int user_id) {
        // 풀이와 하이라이트 정보를 함께 담을 데이터 포맷
        Map<String, Object> data = new HashMap<>();

        // 하이라이트 리스트
        List<CodeHighlightResponseDto> highlightResponseDtos = new ArrayList<>();


        // 팀원의 풀이를 가져오기
        List<Code> codeList = codeQueryRepository.findCodeByTestIdAndProblemIdAndUserId(test_id, problem_id, user_id);

        // 코드 정보와 하이라이트 테이블이 담긴 정보 1개 조회
        Code first = codeList.stream().findFirst().orElseThrow(CodeException::NotFoundCodeException);

        // 제출한 풀이 응답객체 생성
        CodeResponseDto codeResponseDto = CodeResponseDto.builder()
                .submitCode(first.getSubmitCode())
                .language(first.getLanguage())
                .status(first.getStatus())
                .submitAt(first.getSubmitAt())
                .build();


        // 해당 풀이의 하이라이트 리스트 조회
        codeQueryRepository.findHighlightByCodeIdAndUserId((int) first.getId(), user_id).forEach(
                highlight -> {
                    CodeHighlightResponseDto codeHighlightResponseDto = CodeHighlightResponseDto.builder()
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

        return data;
    }
}
