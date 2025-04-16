package programo._pro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chatbot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(name = "test__date", nullable = false)
	private LocalDateTime testDate;

	@Column(nullable = false)
	private String message;

	@Column(name = "send_at")
	private LocalDateTime sendAt;

	public Long getTeamId() {
		return team.getId();
	}
}
