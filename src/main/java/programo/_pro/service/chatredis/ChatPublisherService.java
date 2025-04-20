package programo._pro.service.chatredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatPublisherService {

	private final RedisTemplate<String, String> redisTemplate;

	@Autowired
	public ChatPublisherService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// 채팅방에 메시지를 발행하는 메서드
	public void publishMessage(String chatroomId, String message) {
		redisTemplate.convertAndSend("chatroom:" + chatroomId, message);  // 채널 이름은 chatroom:<채팅방ID>
	}
}