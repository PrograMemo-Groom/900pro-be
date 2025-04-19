package programo._pro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "send_at")
	private ZonedDateTime sendAt;

	@PrePersist
	public void prePersist() {
		this.sendAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
	}
}
