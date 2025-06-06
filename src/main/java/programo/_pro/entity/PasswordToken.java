package programo._pro.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "password_token")
public class PasswordToken {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;


    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 추가
    @JoinColumn(name = "user_id")
    private User user;

    // 사실상 임시 비밀번호
    private String token;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false ;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiredAt = createdAt.plusMinutes(10);
    }




}
