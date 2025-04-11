package programo._pro.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserQueueServiceUnitTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private UserQueueService userQueueService;

    private static final String WAITING = "waiting";
    private static final String PROCESSING = "processing";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        // 기본 설정
    }

    @Test
    @DisplayName("대기열에 사용자 추가 테스트 - 성공")
    void registerWaitingListSuccessTest() {
        // given
        when(redisService.add(eq(WAITING), eq(TEST_EMAIL), anyLong())).thenReturn(true);
        when(redisService.getRank(eq(WAITING), eq(TEST_EMAIL))).thenReturn(5L);

        // when
        Long result = userQueueService.registerWaitingList(TEST_EMAIL);

        // then
        assertEquals(5L, result);
        verify(redisService).add(eq(WAITING), eq(TEST_EMAIL), anyLong());
        verify(redisService).getRank(WAITING, TEST_EMAIL);
    }

    @Test
    @DisplayName("대기열에 사용자 추가 테스트 - 실패")
    void registerWaitingListFailTest() {
        // given
        when(redisService.add(eq(WAITING), eq(TEST_EMAIL), anyLong())).thenReturn(null);

        // when
        Long result = userQueueService.registerWaitingList(TEST_EMAIL);

        // then
        assertEquals(0L, result);
        verify(redisService).add(eq(WAITING), eq(TEST_EMAIL), anyLong());
        verify(redisService, never()).getRank(anyString(), anyString());
    }

    @Test
    @DisplayName("대기열에서 처리 큐로 사용자 이동 테스트 - 처리 큐가 가득 차지 않은 경우")
    void enterWhenProcessingQueueNotFullTest() {
        // given
        when(redisService.count(eq(PROCESSING))).thenReturn(5L);

        // when
        userQueueService.enter();

        // then
        verify(redisService).count(PROCESSING);
        verify(redisService).pop(WAITING, PROCESSING, 5L); // 10 - 5 = 5 (남은 용량)
    }

    @Test
    @DisplayName("대기열에서 처리 큐로 사용자 이동 테스트 - 처리 큐가 가득 찬 경우")
    void enterWhenProcessingQueueFullTest() {
        // given
        when(redisService.count(eq(PROCESSING))).thenReturn(10L);

        // when
        userQueueService.enter();

        // then
        verify(redisService).count(PROCESSING);
        verify(redisService, never()).pop(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("처리 큐에서 사용자 제거 테스트")
    void exitTest() {
        // when
        userQueueService.exit(TEST_EMAIL);

        // then
        verify(redisService).pop(PROCESSING, TEST_EMAIL);
    }
}
