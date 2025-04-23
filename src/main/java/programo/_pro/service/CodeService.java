package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import programo._pro.dto.codeDto.CodeRequestDto;
import programo._pro.dto.codeDto.CodeResponseDto;
import programo._pro.dto.highlightDto.CodeHighlightResponseDto;
import programo._pro.entity.Code;
import programo._pro.entity.CodeHighight;
import programo._pro.global.exception.codeException.CodeException;
import programo._pro.repository.CodeHighlightRepository;
import programo._pro.repository.CodeQueryRepository;
import programo._pro.repository.CodeRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeService {
    private final CodeRepository codeRepository;
    private final CodeQueryRepository codeQueryRepository;
    private final CodeHighlightRepository codeHighlightRepository;


    // 팀원의 첫번째 풀이와 하이라이트 정보를 조회
    public Map<String, Object> getFirstMemberCode(int test_id, int problem_id) {
        // 풀이와 하이라이트 정보를 함께 담을 데이터 포맷
        Map<String, Object> data = new HashMap<>();

        // 팀원의 풀이를 가져오기
        List<Code> codeList = codeQueryRepository.findCodeByTestIdAndProblemId(test_id, problem_id);

        // 팀원의 풀이 리스트 확인
        log.info(codeList.toString());

        // 팀원의 풀이 중 첫번 째 코드 조회
        Code first = codeList.stream().findFirst().orElseThrow(CodeException::NotFoundCodeException);

        // 해당 풀이의 하이라이트 정보 조회
        CodeHighight codeHighight = codeHighlightRepository.findById(first.getId())
                .orElseThrow(CodeException::NotFoundCodeHighlightException);

        // CodeHighlight 응답 객체 생성
        CodeHighlightResponseDto codeHighlightResponseDto = CodeHighlightResponseDto.builder()
                .startPosition(codeHighight.getStartPosition())
                .endPosition(codeHighight.getEndPosition())
                .color(codeHighight.getColor())
                .memo(codeHighight.getMemo())
                .isActive(codeHighight.isActive())
                .build();

        // 제출한 풀이 응답객체 생성
        CodeResponseDto codeResponseDto = CodeResponseDto.builder()
                .submitCode(first.getSubmitCode())
                .language(first.getLanguage())
                .status(first.getStatus())
                .submitAt(first.getSubmitAt())
                .build();

        // Json 추가
        data.put("code", codeResponseDto);
        data.put("highlight", codeHighlightResponseDto);

        return data;
    }
}
