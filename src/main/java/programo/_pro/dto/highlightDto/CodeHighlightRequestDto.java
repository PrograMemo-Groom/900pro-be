package programo._pro.dto.highlightDto;

import lombok.*;
import programo._pro.entity.Color;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeHighlightRequestDto {
    private Long id;          // 수정, 삭제 시 필요
    private Long codeId;      // 코드 ID
    private Long userId;      // 사용자 ID
    private String startPosition;
    private String endPosition;
    private Color color;
    private String memo;
    private boolean isActive;
}
