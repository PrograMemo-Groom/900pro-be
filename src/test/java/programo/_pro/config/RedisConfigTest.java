package programo._pro.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Redis 설정 테스트
 * TestRedisConfiguration을 직접 사용하여 실제 Redis 연결 없이 테스트
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRedisConfiguration.class)
class RedisConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Redis 템플릿 빈 주입 테스트")
    void redisTemplateInjectionTest() {
        // TestRedisConfiguration에서 제공하는 RedisTemplate 빈이 존재하는지 확인
        RedisTemplate<?, ?> redisTemplate = context.getBean(RedisTemplate.class);
        assertNotNull(redisTemplate, "RedisTemplate 빈이 주입되어야 합니다");
    }
}
