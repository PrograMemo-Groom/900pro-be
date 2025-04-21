package programo._pro.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import programo._pro.global.filter.JwtAuthFilter;
import programo._pro.global.filter.JwtAuthenticationFilter;
import programo._pro.repository.UserRepository;
import programo._pro.service.CustomUserDetailsService;
import programo._pro.service.JwtService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserRepository userRepository;

    //   사용자 인증을 처리하는 핵심 매니저 객체를 꺼내서 Bean으로 등록하는 함수
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(provider);
    }

    //    클라이언트가 로그인 요청을 보냈을 때, 아이디/비밀번호를 인증하고 JWT를 생성하는 필터
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);
        filter.setAuthenticationManager(authenticationManager(http));
        return filter;
    }

    // 보안 필터를 아예 적용하지 않을 URL 패턴을 지정합니다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 개발 환경에서는 모든 요청에 대해 필터링을 적용하지 않습니다
        // 운영 환경에서는 절대 사용 금지 ****************************
        return webSecurity -> webSecurity.ignoring().requestMatchers("/**");
    }

    // 전체 HTTP 보안 정책과 필터 체인을 정의하는 핵심 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWTs를 사용하는 REST API, CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        // websocket 경로는 permitAll()로 명시
                        .requestMatchers("/ws-chat/**").permitAll()
                        // 개발환경에서는 모든 요청 허용 -> 추후 운영서버는 수정
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class) // JWT 검증 필터 삽입
                .addFilterAfter(new JwtAuthFilter(customUserDetailsService, jwtService), UsernamePasswordAuthenticationFilter.class) // JWT 검증 필터 삽입
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
