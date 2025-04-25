package programo._pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import programo._pro.dto.teamDto.TeamCreateRequest;

import java.time.LocalDateTime;

@Entity
@Table(name = "team")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "team_name", nullable = false, length = 10) // 10자로 길이 제한
	private String teamName;

	@Column(name = "description", length = 700) // 최대 700자, nullable
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false)
	private Level level;  // ENUM

	@Column(name = "problem_count", nullable = false)
	private int problemCount;

	@Column(name = "start_time", nullable = false)
	private String startTime;

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
	@Builder.Default
	private boolean isActive = true;

	@Column(name = "is_chat_sent")
	@Builder.Default
	private boolean isChatSent = false;

	//도메인메서드 : setter 대신 사용
	//teamUpdate가 TeamCreateRequest와 같은 포멧이라 dto 재활용하겠습니당
	public void updateInfo(TeamCreateRequest req) {
		this.teamName = req.getTeamName();
		this.description = req.getDescription();
		this.level = req.getLevel();
		this.problemCount = req.getProblemCount();
		this.startTime = req.getStartTime();
		this.durationTime = req.getDurationTime();
	}

	public void setNotActive() {
		this.isActive = false;
	}

}
