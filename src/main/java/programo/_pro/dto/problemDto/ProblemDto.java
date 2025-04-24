package programo._pro.dto.problemDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import programo._pro.entity.Level;

import java.time.LocalDateTime;

@Builder
@Getter
public class ProblemDto {
    private int problemId;
    private int baekNum;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private String exInput;
    private String exOutput;
    private String inputDes;
    private String outputDes;
    private int timeLimit;
    private int memoryLimit;
}
