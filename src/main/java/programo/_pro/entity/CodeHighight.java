package programo._pro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "code_highlight")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeHighight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    @Column(name = "start_pos")
    private String startPosition;

    @Column(name = "end_pos")
    private String endPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private Color color;

    @Column(name = "memo")
    private String memo;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;
}
