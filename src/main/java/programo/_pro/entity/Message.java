package programo._pro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_bot", nullable = false)
	private boolean isBot = false;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void setCreatedAt() {
		this.createdAt = LocalDateTime.now();
	}
}
