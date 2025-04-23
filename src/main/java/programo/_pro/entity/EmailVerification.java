package programo._pro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_verification")
@Entity
@Builder
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private int code;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // 현재 시간
        expiredAt = LocalDateTime.now().plusMinutes(10); // 만료시간 10분으로 설정
    }
}
