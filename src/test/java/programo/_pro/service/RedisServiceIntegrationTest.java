package programo._pro.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

import programo._pro.Application;
import programo._pro.config.RedisConfig;

/**
 * RedisService 통합 테스트 - 실제 Redis 인스턴스 사용
 */
@SpringBootTest(classes = {Application.class, RedisConfig.class})
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class RedisServiceIntegrationTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final String testKey = "testKey";
    private final String testValue = "testValue";
    private final String testQueueName = "testQueue";
    private final long testScore = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 전 기존 데이터 정리
        redisTemplate.delete(testKey);
        redisTemplate.delete(testQueueName);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        redisTemplate.delete(testKey);
        redisTemplate.delete(testQueueName);
    }

    @Test
    @DisplayName("Set에 값 추가 및 조회 통합 테스트")
    void addAndGetFromSetTest() {
        // when - 값 추가
        Long result = redisService.add(testKey, testValue);

        // then - 추가 결과 확인
        assertEquals(1L, result);

        // when - 값 조회
        String retrievedValue = redisService.getValue(testKey);

        // then - 조회 결과 확인
        assertEquals(testValue, retrievedValue);
    }

    @Test
    @DisplayName("ZSet에 값 추가 및 순위 조회 통합 테스트")
    void addAndGetRankFromZSetTest() {
        // when - 값 추가
        Boolean result = redisService.add(testQueueName, testKey, testScore);

        // then - 추가 결과 확인
        assertTrue(result);

        // when - 순위 조회
        long rank = redisService.getRank(testQueueName, testKey);

        // then - 순위 확인
        assertEquals(0L, rank);
    }

    @Test
    @DisplayName("ZSet에 여러 값 추가 및 크기 확인 통합 테스트")
    void addMultipleItemsAndCountZSetTest() {
        // when - 여러 값 추가
        redisService.add(testQueueName, "item1", 1L);
        redisService.add(testQueueName, "item2", 2L);
        redisService.add(testQueueName, "item3", 3L);

        // then - ZSet 크기 확인
        Long size = redisService.count(testQueueName);
        assertEquals(3L, size);
    }

    @Test
    @DisplayName("ZSet에서 최소값 팝 테스트 통합 테스트")
    void popMinFromZSetTest() {
        // given - 테스트 데이터 준비
        String processingQueue = "processingQueue";
        redisService.add(testQueueName, "item1", 1L);
        redisService.add(testQueueName, "item2", 2L);

        // when - pop 실행
        redisService.pop(testQueueName, processingQueue, 1L);

        // then - 결과 확인
        assertEquals(1L, redisService.count(testQueueName));
        assertEquals(1L, redisService.count(processingQueue));
    }

    @Test
    @DisplayName("ZSet에서 특정 키 제거 통합 테스트")
    void removeFromZSetByKeyTest() {
        // given - 테스트 데이터 준비
        redisService.add(testQueueName, testKey, testScore);

        // when - 키 제거
        redisService.pop(testQueueName, testKey);

        // then - 조회 시 예외 발생 확인
        assertThrows(NoSuchElementException.class, () -> {
            redisService.getRank(testQueueName, testKey);
        });
    }

    @Test
    @DisplayName("키 삭제 통합 테스트")
    void deleteValueTest() {
        // given - 테스트 데이터 준비
        redisService.add(testKey, testValue);

        // when - 키 삭제
        redisService.deleteValue(testKey);

        // then - 키 존재 여부 확인
        assertFalse(redisTemplate.hasKey(testKey));
    }
}
