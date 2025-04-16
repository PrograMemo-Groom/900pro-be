package programo._pro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programo._pro.entity.Level;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TeamCreateRequest {

    @NotBlank
    private String teamName;

    private String description;

    @NotNull
    private Level level;

    @Min(3)
    @Max(5)
    private int problemCount;

    @NotNull
    private LocalDateTime startTime;

    @Min(2)
    @Max(5)
    private int durationTime;
}
