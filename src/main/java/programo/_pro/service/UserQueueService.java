package programo._pro.service;

import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueueService {
    private static final long MAXIMUM_CAPACITY = 10;
    private static final String WAITING = "waiting";
    private static final String PROCESSING = "processing";
    private final RedisService redisService;

    public Long registerWaitingList(long userId) {
        long unixTimestamp = Instant.now().getEpochSecond();
        Boolean result = redisService.add(WAITING, Long.toString(userId), unixTimestamp);
        if(Objects.nonNull(result))
            return redisService.getRank(WAITING, Long.toString(userId));
        return 0L;
    }

    public void enter(){
        long count = calculateCapacity();
        if(count > 0)
            redisService.pop(WAITING, PROCESSING, count);
        else log.info("Current Processing Queue Size is Maximum!");
    }

    public void exit(Long userId){
        redisService.pop(PROCESSING, userId);
    }

    private long calculateCapacity() {
        Long currentCount = redisService.count(PROCESSING);
        return MAXIMUM_CAPACITY - currentCount;
    }
}
