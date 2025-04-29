package programo._pro.config.jwt;

import com.querydsl.core.annotations.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("[WebSocket] STOMP CONNECT 요청 감지");

            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.warn("[WebSocket] Authorization 헤더 없음 또는 형식 오류");
                throw new IllegalArgumentException("Authorization 헤더 없음");
            }

            String token = authorizationHeader.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[WebSocket] JWT 토큰 유효하지 않음");
                throw new IllegalArgumentException("JWT 토큰 유효하지 않음");
            }

            Long userId = jwtTokenProvider.getUserId(token);

            log.info("[WebSocket] JWT 인증 성공, userId={}", userId);

            // 사용자 인증 정보를 WebSocket 세션에 저장 (필요하면 Principal 설정도 가능)
            accessor.setUser(new StompPrincipal(String.valueOf(userId)));
        }

        return message;
    }
}
