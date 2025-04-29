package programo._pro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import programo._pro.config.jwt.JwtChannelInterceptor;
import programo._pro.config.jwt.JwtHandshakeInterceptor;
import programo._pro.config.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

//	private final JwtTokenProvider jwtTokenProvider;
private final JwtChannelInterceptor jwtChannelInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-chat")
//				.addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider))
				.setAllowedOriginPatterns("*")
				.withSockJS(); // SockJs 추가
	}

	//대신, Spring 서버 메모리 기반(SimpleBroker)로 임시 전환
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub"); // 메모리 기반 브로커
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(jwtChannelInterceptor); // ✅ 여기서 JwtChannelInterceptor 추가
	}
}