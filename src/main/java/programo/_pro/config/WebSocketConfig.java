package programo._pro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import programo._pro.config.jwt.JwtHandshakeInterceptor;
import programo._pro.config.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-chat")
				.addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider))
				.setAllowedOriginPatterns("*");
	}

	//대신, Spring 서버 메모리 기반(SimpleBroker)로 임시 전환
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub"); // 메모리 기반 브로커
		registry.setApplicationDestinationPrefixes("/pub");
	}

	//외부 메시지 브로커 사용코드임 : 일단 Redis 안쓰고 웹소켓 테스트 !!!!!!!!!!
//	@Override
//	public void configureMessageBroker(MessageBrokerRegistry registry) {
//		// Reids 메시지 브로커 설정
//		registry.enableStompBrokerRelay("/sub") // "/sub" 접두사로 들어오는 메시지를 Redis에서 발행
//				.setRelayHost("localhost")  // Redis 호스트 설정
//				.setRelayPort(6379)         // Redis 포트 (기본값: 6379)
//				.setClientLogin("user")     // Redis 클라이언트 로그인
//				.setClientPasscode("pass") // Redis 클라이언트 패스워드
//				.setSystemLogin("system")  // 시스템 로그인
//				.setSystemPasscode("system-pass"); // 시스템 패스워드 설정
//
//		// 클라이언트에서 전송되는 메시지의 접두사 설정
//		registry.setApplicationDestinationPrefixes("/pub");
//	}
}
