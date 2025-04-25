//package programo._pro.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SetOperations;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
//
//import java.util.HashSet;
//import java.util.NoSuchElementException;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
///**
// * RedisService에 대한 유닛 테스트
// */
//@ExtendWith(MockitoExtension.class)
//class RedisServiceUnitTest {
//
//    @Mock
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Mock
//    private SetOperations<String, String> setOperations;
//
//    @Mock
//    private ZSetOperations<String, String> zSetOperations;
//
//    @InjectMocks
//    private RedisService redisService;
//
//    private final String testKey = "testKey";
//    private final String testValue = "testValue";
//    private final String testQueueName = "testQueue";
//    private final long testScore = 1L;
//
//    @Test
//    @DisplayName("Set에 값 추가 테스트")
//    void addToSetTest() {
//        // given
//        when(redisTemplate.opsForSet()).thenReturn(setOperations);
//        when(setOperations.add(testKey, testValue)).thenReturn(1L);
//
//        // when
//        Long result = redisService.add(testKey, testValue);
//
//        // then
//        assertEquals(1L, result);
//        verify(setOperations).add(testKey, testValue);
//    }
//
//    @Test
//    @DisplayName("Set에서 값 가져오기 테스트")
//    void getValueFromSetTest() {
//        // given
//        when(redisTemplate.opsForSet()).thenReturn(setOperations);
//        when(setOperations.pop(testKey)).thenReturn(testValue);
//
//        // when
//        String result = redisService.getValue(testKey);
//
//        // then
//        assertEquals(testValue, result);
//        verify(setOperations).pop(testKey);
//    }
//
//    @Test
//    @DisplayName("ZSet에 값 추가 테스트")
//    void addToZSetTest() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.add(testQueueName, testKey, testScore)).thenReturn(true);
//
//        // when
//        Boolean result = redisService.add(testQueueName, testKey, testScore);
//
//        // then
//        assertTrue(result);
//        verify(zSetOperations).add(testQueueName, testKey, testScore);
//    }
//
//    @Test
//    @DisplayName("ZSet에서 순위 가져오기 테스트")
//    void getRankFromZSetTest() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.rank(testQueueName, testKey)).thenReturn(0L);
//
//        // when
//        long result = redisService.getRank(testQueueName, testKey);
//
//        // then
//        assertEquals(0L, result);
//        verify(zSetOperations).rank(testQueueName, testKey);
//    }
//
//    @Test
//    @DisplayName("ZSet에서 존재하지 않는 키 조회 시 예외 발생 테스트")
//    void getRankNonExistingKeyTest() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.rank(testQueueName, testKey)).thenReturn(null);
//
//        // when & then
//        assertThrows(NoSuchElementException.class, () -> {
//            redisService.getRank(testQueueName, testKey);
//        });
//    }
//
//    @Test
//    @DisplayName("ZSet 크기 확인 테스트")
//    void countZSetSizeTest() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.size(testQueueName)).thenReturn(10L);
//
//        // when
//        Long result = redisService.count(testQueueName);
//
//        // then
//        assertEquals(10L, result);
//        verify(zSetOperations).size(testQueueName);
//    }
//
//    @Test
//    @DisplayName("ZSet에서 최소값 팝 테스트 - 결과가 존재하는 경우")
//    void popMinFromZSetWhenResultExistsTest() {
//        // given
//        String processingQueue = "processingQueue";
//        long count = 5L;
//        Set<TypedTuple<String>> resultSet = new HashSet<>();
//        @SuppressWarnings("unchecked")
//        TypedTuple<String> typedTuple = mock(TypedTuple.class);
//        when(typedTuple.getValue()).thenReturn(testKey);
//        resultSet.add(typedTuple);
//
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.popMin(testQueueName, count)).thenReturn(resultSet);
//        when(zSetOperations.add(processingQueue, testKey, 1L)).thenReturn(true);
//
//        // when
//        redisService.pop(testQueueName, processingQueue, count);
//
//        // then
//        verify(zSetOperations).popMin(testQueueName, count);
//        verify(zSetOperations).add(processingQueue, testKey, 1L);
//    }
//
//    @Test
//    @DisplayName("ZSet에서 최소값 팝 테스트 - 결과가 없는 경우")
//    void popMinFromZSetWhenResultNotExistsTest() {
//        // given
//        String processingQueue = "processingQueue";
//        long count = 5L;
//
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.popMin(testQueueName, count)).thenReturn(null);
//
//        // when
//        redisService.pop(testQueueName, processingQueue, count);
//
//        // then
//        verify(zSetOperations).popMin(testQueueName, count);
//        verify(zSetOperations, never()).add(anyString(), anyString(), anyLong());
//    }
//
//    @Test
//    @DisplayName("ZSet에서 ID로 값 제거 테스트")
//    void removeFromZSetByIdTest() {
//        // given
//        long id = 123L;
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//
//        // when
//        redisService.pop(testQueueName, id);
//
//        // then
//        verify(zSetOperations).remove(testQueueName, Long.toString(id));
//    }
//
//    @Test
//    @DisplayName("ZSet에서 키로 값 제거 테스트")
//    void removeFromZSetByKeyTest() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//
//        // when
//        redisService.pop(testQueueName, testKey);
//
//        // then
//        verify(zSetOperations).remove(testQueueName, testKey);
//    }
//
//    @Test
//    @DisplayName("키 삭제 테스트")
//    void deleteValueTest() {
//        // when
//        redisService.deleteValue(testKey);
//
//        // then
//        verify(redisTemplate).delete(testKey);
//    }
//}
