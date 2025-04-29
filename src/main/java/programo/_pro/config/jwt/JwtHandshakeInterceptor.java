package programo._pro.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request,
								   ServerHttpResponse response,
								   WebSocketHandler webSocketHandler,
								   Map<String, Object> attributes) throws Exception{

		log.info("[WebSocket] 클라이언트 연결 시도");

		HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
		String token = servletRequest.getParameter("token");

		// 프론트측에서 /ws-chat으로 연결 테스트해야 나타나는 로그
		log.info("[WebSocket] Attempting handshake...");
		log.info("[WebSocket] Received token: {}", token);

		if (token != null && jwtTokenProvider.validateToken(token)) {
			String userId = jwtTokenProvider.getUserIdFromToken(token);
			attributes.put("userId", userId);
			log.info("[WebSocket] JWT validation successful. userId: {}", userId);
			return true;
		}
		log.warn("[WebSocket] JWT validation failed. Connection denied.");

		/*
		String authHeader = servletRequest.getHeader("Authorization");
		log.info("[WebSocket] Authorization 헤더: {}", authHeader);

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			if (jwtTokenProvider.validateToken(token)) {
				String userId = jwtTokenProvider.getUserIdFromToken(token);
				attributes.put("userId", userId);
				log.info("[WebSocket] JWT 유효함 ✅ userId: {}", userId);
				return true;
			}
		}

		log.warn("[WebSocket] JWT 유효하지 않음 ❌");
		 */
		return false;

	}

	@Override
	public void afterHandshake(ServerHttpRequest request,
							   ServerHttpResponse response,
							   WebSocketHandler wsHandler,
							   Exception exception) {
		log.info("[WebSocket] 클라이언트 연결 완료");
	}


}
