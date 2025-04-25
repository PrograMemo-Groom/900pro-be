package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.highlightDto.CodeHighlightRequestDto;
import programo._pro.dto.highlightDto.CodeHighlightResponseDto;
import programo._pro.entity.Code;
import programo._pro.entity.CodeHighight;
import programo._pro.entity.User;
import programo._pro.global.exception.codeException.CodeException;
import programo._pro.global.exception.highlightException.HighlightException;
import programo._pro.global.exception.userException.UserException;
import programo._pro.repository.CodeHighlightRepository;
import programo._pro.repository.CodeRepository;
import programo._pro.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeHighlightService {
    private final CodeHighlightRepository codeHighlightRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

    // 하이라이트 추가
    @Transactional
    public CodeHighlightResponseDto addHighlight(CodeHighlightRequestDto requestDto) {
        // 관련 엔티티 조회
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(UserException::byId);
        Code code = codeRepository.findById(requestDto.getCodeId())
                .orElseThrow(CodeException::NotFoundCodeException);

        // 하이라이트 엔티티 생성
        CodeHighight highlight = CodeHighight.builder()
                .user(user)
                .code(code)
                .startPosition(requestDto.getStartPosition())
                .endPosition(requestDto.getEndPosition())
                .color(requestDto.getColor())
                .memo(requestDto.getMemo())
                .isActive(requestDto.isActive())
                .build();

        // 저장
        CodeHighight savedHighlight = codeHighlightRepository.save(highlight);

        // 응답 DTO 반환
        return CodeHighlightResponseDto.builder()
                .startPosition(savedHighlight.getStartPosition())
                .endPosition(savedHighlight.getEndPosition())
                .color(savedHighlight.getColor())
                .memo(savedHighlight.getMemo())
                .isActive(savedHighlight.isActive())
                .build();
    }

    // 하이라이트 수정
    @Transactional
    public CodeHighlightResponseDto updateHighlight(CodeHighlightRequestDto requestDto, Long highlightId) {
        // 하이라이트 조회
        CodeHighight highlight = codeHighlightRepository.findById(highlightId)
                .orElseThrow(HighlightException::NotFoundHighlightException);

        // 값 업데이트
        highlight.setStartPosition(requestDto.getStartPosition());
        highlight.setEndPosition(requestDto.getEndPosition());
        highlight.setColor(requestDto.getColor());
        highlight.setMemo(requestDto.getMemo());
        highlight.setActive(requestDto.isActive());

        // 저장
        CodeHighight updatedHighlight = codeHighlightRepository.save(highlight);

        // 응답 DTO 반환
        return CodeHighlightResponseDto.builder()
                .startPosition(updatedHighlight.getStartPosition())
                .endPosition(updatedHighlight.getEndPosition())
                .color(updatedHighlight.getColor())
                .memo(updatedHighlight.getMemo())
                .isActive(updatedHighlight.isActive())
                .build();
    }

    // 하이라이트 삭제 (soft delete)
    @Transactional
    public void deleteHighlight(Long highlightId) {
        // 하이라이트 조회
        CodeHighight highlight = codeHighlightRepository.findById(highlightId)
                .orElseThrow(HighlightException::NotFoundHighlightException);

        // isActive를 false로 설정하여 soft delete 구현
        highlight.setActive(false);

        // 저장
        codeHighlightRepository.save(highlight);
    }
}
