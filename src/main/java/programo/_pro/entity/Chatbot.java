package programo._pro.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "chatbot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chatbot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "챗봇 메시지 ID", example = "1")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	@Schema(description = "소속 팀 ID", example = "101")
	private Team team;

	@Column(name = "test_date", nullable = false)
	@Schema(description = "테스트 날짜", example = "2025-04-10")
	private ZonedDateTime testDate;

	@Column(name = "message", nullable = false)
	@Schema(description = "챗봇 메시지 내용", example = "응시하느라 고생하셨습니다!")
	private String message;

	@Column(name = "send_at")
	@Schema(description = "메시지 전송 시간", example = "2025-04-10T15:44:00")
	private ZonedDateTime sendAt;

	// 챗봇과 문제 연결하는 컬럼은 추후, problem테이블 만들어진 후 생성해야함
//	@ManyToMany
//	@JoinTable(
//			name = "chatbot_problem",
//			joinColumns = @JoinColumn(name = "chatbot_id"),
//			inverseJoinColumns = @JoinColumn(name = "problem_id")
//	)

	@PrePersist
	public void prePersist() {
		this.sendAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
	}

	public Long getTeamId() {
		return team.getId();
	}

	public void setTeamId(Long teamId) {
	}
}
