package programo._pro.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chatroom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "채팅방 ID", example = "1")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false, unique = true)
	@Schema(description = "소속 팀 ID", example = "101")
	private Team team;
}
