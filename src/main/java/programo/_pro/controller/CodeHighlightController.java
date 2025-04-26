package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.highlightDto.CodeHighlightRequestDto;
import programo._pro.dto.highlightDto.CodeHighlightResponseDto;
import programo._pro.global.ApiResponse;
import programo._pro.service.CodeHighlightService;

@RestController
@RequestMapping("/api/highlight")
@RequiredArgsConstructor
@Tag(name = "코드 하이라이트 API", description = "코드 하이라이트 관련 API")
public class CodeHighlightController {

    private final CodeHighlightService codeHighlightService;

    @Operation(summary = "하이라이트 추가", description = "코드에 새로운 하이라이트를 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CodeHighlightResponseDto>> addHighlight(
            @RequestBody CodeHighlightRequestDto requestDto) {
        CodeHighlightResponseDto responseDto = codeHighlightService.addHighlight(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto, "하이라이트 추가 성공"));
    }

    @Operation(summary = "하이라이트 수정", description = "기존 하이라이트 정보를 수정합니다.")
    @PatchMapping("/{highlightId}")
    public ResponseEntity<ApiResponse<CodeHighlightResponseDto>> updateHighlight(
            @PathVariable("highlightId") Long highlightId,
            @RequestBody CodeHighlightRequestDto requestDto) {
        CodeHighlightResponseDto responseDto = codeHighlightService.updateHighlight(requestDto, highlightId);
        return ResponseEntity.ok(ApiResponse.success(responseDto, "하이라이트 수정 성공"));
    }

    @Operation(summary = "하이라이트 삭제", description = "하이라이트를 비활성화(soft delete)합니다.")
    @DeleteMapping("/{highlightId}")
    public ResponseEntity<ApiResponse<Void>> deleteHighlight(@PathVariable Long highlightId) {
        codeHighlightService.deleteHighlight(highlightId);
        return ResponseEntity.ok(ApiResponse.success(null, "하이라이트 삭제 성공"));
    }
}
