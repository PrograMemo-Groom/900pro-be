package programo._pro.dto.codeDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import programo._pro.entity.Problem;
import programo._pro.entity.Status;
import programo._pro.entity.Test;
import programo._pro.entity.User;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Setter
@Getter
public class CodeResponseDto {

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "submit_code")
    private String submitCode;

    @Column(name = "submit_at")
    private LocalDateTime submitAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "응시자 상태", example = "IN_PROGRESS, COMPLETED, ABSENT")
    private Status status;
}
