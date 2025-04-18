package programo._pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {
    private String status;
    private String stdout;
    private Integer exitCode;

    // 에러 정보 필드
    private ErrorInfo error;

    @Data
    @Builder
    public static class ErrorInfo {
        private String code;       // 에러 코드
        private String message;    // 사용자 친화적 메시지
        private String detail;     // 상세 에러 정보 (개발자용)
        private String source;     // 에러 출처 (container, system 등)
    }
}
