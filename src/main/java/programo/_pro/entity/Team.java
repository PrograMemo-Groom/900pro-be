package programo._pro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "team_name", nullable = false)
	private String teamName;

	@Column(name = "description")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false)
	private Level level;  // ENUM

	@Column(name = "problem_count", nullable = false)
	private int problemCount;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "duration_time", nullable = false)
	private int durationTime;

	@Column(name = "current_members", nullable = false)
	private int currentMembers;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "leader_id", nullable = false)
	private User leader;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "is_active")
	private boolean isActive = true;
}
