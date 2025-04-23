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

    private String language;
    private String submitCode;
    private LocalDateTime submitAt;
    private Status status;
}
