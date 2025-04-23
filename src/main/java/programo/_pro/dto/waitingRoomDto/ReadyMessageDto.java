package programo._pro.dto.waitingRoomDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadyMessageDto {
    private Long teamId;      // 팀 고유 ID
    private Long userId;      // 유저 고유 ID
    private String userName;  // 유저 이름
    private String status;    // "READY" , "WAITING"
}
