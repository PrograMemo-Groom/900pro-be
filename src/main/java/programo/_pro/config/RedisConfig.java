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

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    private final SimpMessagingTemplate messagingTemplate;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // 일반적인 key:value의 경우 시리얼라이저
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // Hash를 사용할 경우 시리얼라이저
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        // 모든 경우
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    @Bean
    public MessageListener messageListener() {
        return new MessageListenerAdapter(new ChatMessageListener(messagingTemplate), "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                  MessageListener messageListener) {
        // RedisMessageListenerContainer 설정
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // "chatroom" 채널에서 메시지를 수신
        container.addMessageListener(messageListener, new PatternTopic("chatroom"));
        return container;
    }

    @Bean
    public ChatMessageListener chatMessageListener() {
        return new ChatMessageListener(messagingTemplate);
    }
}
