package programo._pro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import programo._pro.service.chatredis.ChatMessageListener;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    // application.properties 파일에서 Redis 호스트와 포트를 주입받음
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    // Spring WebSocket을 통해 메시지를 전송할 때 사용
    private final SimpMessagingTemplate messagingTemplate;

    // RedisConnectionFactory 빈 생성: LettuceConnectionFactory를 사용해 Redis와 연결
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Lettuce는 비동기/반응형 Redis 클라이언트로, 호스트와 포트를 이용하여 Redis에 연결
        return new LettuceConnectionFactory(host, port);
    }

    // RedisTemplate 빈 생성: Redis와 상호작용하는 주요 객체로, Redis에 데이터를 저장하거나 조회할 때 사용
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // RedisConnectionFactory로부터 Redis 연결을 설정
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // 일반적인 key:value의 경우 시리얼라이저
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // 키를 문자열로 직렬화
        redisTemplate.setValueSerializer(new StringRedisSerializer()); // 값도 문자열로 직렬화

        // Hash를 사용할 경우 시리얼라이저
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        // 모든 데이터에 대해 기본적으로 문자열 시리얼라이저를 사용하도록 설정
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());

        // 설정한 RedisTemplate 객체를 반환
        return redisTemplate;
    }

    // Redis에서 수신한 메시지를 처리할 MessageListener 빈 생성
    @Bean
    public MessageListener messageListener() {
        // ChatMessageListener는 Redis에서 메시지를 수신했을 때 처리할 리스너
        return new MessageListenerAdapter(new ChatMessageListener(messagingTemplate), "onMessage");
        // 메시지가 수신되면 "onMessage" 메서드를 호출하여 처리
    }

    // RedisMessageListenerContainer 빈 생성: Redis에서 메시지를 수신할 컨테이너
    @Bean
    public RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                  MessageListener messageListener) {
        // RedisMessageListenerContainer는 Redis에서 오는 메시지를 처리하는 중심 객체
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory); // RedisConnectionFactory 설정

        // "chatroom" 채널에서 발생하는 메시지를 수신하도록 설정
        container.addMessageListener(messageListener, new PatternTopic("chatroom"));

        // 설정된 RedisMessageListenerContainer 객체를 반환
        return container;
    }

    // ChatMessageListener 빈 생성: Redis로부터 메시지를 수신할 때 처리하는 실제 로직을 담당하는 리스너
    @Bean
    public ChatMessageListener chatMessageListener() {

        return new ChatMessageListener(messagingTemplate);
        // ChatMessageListener는 메시지를 받아 SimpMessagingTemplate을 사용해 WebSocket 클라이언트로 전달
    }
}
