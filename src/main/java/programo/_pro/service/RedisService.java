package programo._pro.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public Long add(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public String getValue(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    public Boolean add(String queueName, String key, long value) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(queueName, key, value));
    }

    public long getRank(String queueName, String key) {
        return Optional.ofNullable(redisTemplate.opsForZSet().rank(queueName, key))
                .orElseThrow(() -> new NoSuchElementException("No such id: " + key));
    }

    public void contains(String queueName, String key) {
        getRank(queueName, key);
    }

    public void pop(String waitingQueue, String processingQueue, long count) {
        Set<ZSetOperations.TypedTuple<String>> result = redisTemplate.opsForZSet().popMin(waitingQueue, count);
        if (Objects.nonNull(result)) {
            result.forEach(key -> this.add(processingQueue, key.getValue(), 1L));
        }
    }

    public void pop(String processingQueue, long id) {
        redisTemplate.opsForZSet().remove(processingQueue, Long.toString(id));
    }

    public Long count(String queueName) {
        return redisTemplate.opsForZSet().size(queueName);
    }

}
