package programo._pro.dto.highlightDto;

import lombok.*;
import programo._pro.entity.Color;

@Setter
@Getter
@Builder
public class CodeHighlightResponseDto {
    private String startPosition;
    private String endPosition;
    private Color color;
    private String memo;
    private boolean isActive;
}
