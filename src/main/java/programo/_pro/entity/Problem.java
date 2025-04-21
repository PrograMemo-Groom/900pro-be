package programo._pro.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "problem")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "문제 id", example = "20")
    private long id;

    @Column(name = "baek_num", nullable = false)
    @Schema(description = "해당 id의 백준문제번호", example = "3044")
    private int baekNum;


    @Column(name = "title", nullable = false)
    @Schema(description = "문제 이름", example = "A+B 문제")
    private String title;

    @Column(name = "description", nullable = false)
    @Schema(description = "문제 설명", example = "A+B를 구하시오")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    @Schema(description = "문제의 난이도", example = "EASY, MEDIUM, HARD")
    private Level level;

    @Column(name = "ex_input", nullable = false)
    @Schema(description = "예제 입력값")
    private String exInput;

    @Column(name = "ex_output", nullable = false)
    @Schema(description = "예제 출력값")
    private String exOutput;


    @Column(name = "time_limit", nullable = false)
    @Schema(description = "문제의 시간제한")
    private int timeLimit;

    @Column(name = "memory_limit", nullable = false)
    @Schema(description = "문제의 메모리 제한")
    private int memoryLimit;
}
