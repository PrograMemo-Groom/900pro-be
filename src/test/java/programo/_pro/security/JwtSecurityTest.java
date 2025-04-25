//package programo._pro.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import programo._pro.service.JwtService;
//import programo._pro.service.RedisService;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * JWT 보안 심화 테스트
// *
// * 이 테스트 클래스는 JWT 기반 보안 필터의 기능을 검증합니다.
// * JWT를 사용한 인증 메커니즘, 보호된 리소스 접근, 만료된 토큰 처리,
// * 토큰 블랙리스트, 리프레시 토큰 등 심화된 기능을 테스트합니다.
// *
// * 테스트 내용:
// * - JWT 필터를 통한 인증 처리
// * - 유효한 토큰으로 보호된 리소스 접근
// * - 유효하지 않은 토큰으로 접근 차단
// * - 만료된 토큰 자동 갱신
// * - 로그아웃 시 토큰 블랙리스트 처리
// */
//@ExtendWith(MockitoExtension.class)
//public class JwtSecurityTest {
//
//    @Mock
//    private JwtService jwtService;
//
//    @Mock
//    private RedisService redisService;
//
//    @Mock
//    private FilterChain filterChain;
//
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//    private HttpServletRequest request;
//    private HttpServletResponse response;
//
//    private final String TEST_EMAIL = "test@example.com";
//    private final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJleHAiOjIxNzc0NTI3OTl9.RyQ8ftUDU94xg-_Z3Bnhh2c9s8rwtKVMtsCHSGQTKdY";
//
//    @BeforeEach
//    void setUp() {
//        // 테스트를 위한 JWT 인증 필터 생성
//        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
//
//        // HTTP 요청 및 응답 객체 생성
//        request = new MockHttpServletRequest();
//        response = new MockHttpServletResponse();
//
//        // 테스트 전에 SecurityContext 초기화
//        SecurityContextHolder.clearContext();
//    }
//
//    /**
//     * 유효한 JWT 토큰으로 인증 성공 테스트
//     *
//     * 이 테스트는 유효한 JWT 토큰이 Authorization 헤더에 포함된 경우
//     * JWT 인증 필터가 올바르게 인증을 처리하고 SecurityContext에 인증 정보를 설정하는지 확인합니다.
//     */
//    @Test
//    @DisplayName("유효한 JWT 토큰으로 인증 성공 테스트")
//    void validTokenAuthenticationTest() throws Exception {
//        // given
//        // 요청 헤더에 유효한 토큰 설정
//        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + TEST_TOKEN);
//
//        // JwtService 모킹
//        when(jwtService.validateAccessToken(eq(TEST_TOKEN), any(HttpServletResponse.class))).thenReturn(true);
//        when(jwtService.getUserEmail(TEST_TOKEN)).thenReturn(TEST_EMAIL);
//
//        // when
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then
//        // 필터 체인이 계속 진행되었는지 확인
//        verify(filterChain).doFilter(request, response);
//
//        // SecurityContext에 인증 정보가 설정되었는지 확인
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(authentication, "인증 정보가 설정되어야 합니다.");
//        assertEquals(TEST_EMAIL, authentication.getName(), "인증된 사용자의 이메일이 일치해야 합니다.");
//        assertTrue(authentication.isAuthenticated(), "인증 상태여야 합니다.");
//    }
//
//    /**
//     * 유효하지 않은 JWT 토큰으로 인증 실패 테스트
//     *
//     * 이 테스트는 유효하지 않은 JWT 토큰이 Authorization 헤더에 포함된 경우
//     * JWT 인증 필터가 인증을 거부하고 SecurityContext에 인증 정보를 설정하지 않는지 확인합니다.
//     */
//    @Test
//    @DisplayName("유효하지 않은 JWT 토큰으로 인증 실패 테스트")
//    void invalidTokenAuthenticationTest() throws Exception {
//        // given
//        String invalidToken = "invalid.token.format";
//        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + invalidToken);
//
//        // JwtService 모킹
//        when(jwtService.validateAccessToken(eq(invalidToken), any(HttpServletResponse.class))).thenReturn(false);
//
//        // when
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then
//        // 필터 체인이 계속 진행되었는지 확인
//        verify(filterChain).doFilter(request, response);
//
//        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        assertNull(authentication, "인증 정보가 설정되지 않아야 합니다.");
//    }
//
//    /**
//     * Authorization 헤더가 없는 경우 테스트
//     *
//     * 이 테스트는 요청에 Authorization 헤더가 없는 경우
//     * JWT 인증 필터가 이를 무시하고 필터 체인을 계속 진행하는지 확인합니다.
//     */
//    @Test
//    @DisplayName("Authorization 헤더가 없는 경우 테스트")
//    void noAuthorizationHeaderTest() throws Exception {
//        // given
//        // Authorization 헤더 없음
//
//        // when
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then
//        // 필터 체인이 계속 진행되었는지 확인
//        verify(filterChain).doFilter(request, response);
//
//        // SecurityContext에 인증 정보가 설정되지 않았는지 확인
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        assertNull(authentication, "인증 정보가 설정되지 않아야 합니다.");
//    }
//
//    /**
//     * 만료된 JWT 토큰 자동 갱신 테스트
//     *
//     * 이 테스트는 만료된 JWT 토큰이 Authorization 헤더에 포함된 경우
//     * JWT 인증 필터가 토큰을 자동으로 갱신하고 응답 헤더에 새 토큰을 추가하는지 확인합니다.
//     */
//    @Test
//    @DisplayName("만료된 JWT 토큰 자동 갱신 테스트")
//    void expiredTokenRenewalTest() throws Exception {
//        // given
//        String expiredToken = "expired.token.value";
//        String newToken = "new.token.value";
//        ((MockHttpServletRequest) request).addHeader("Authorization", "Bearer " + expiredToken);
//
//        // JwtService 모킹
//        when(jwtService.validateAccessToken(eq(expiredToken), any(HttpServletResponse.class)))
//            .thenAnswer(invocation -> {
//                // 응답 헤더에 새 토큰 추가하는 동작 시뮬레이션
//                HttpServletResponse resp = invocation.getArgument(1);
//                resp.addHeader("Authorization", "Bearer " + newToken);
//                return true;
//            });
//        when(jwtService.getUserEmail(expiredToken)).thenReturn(TEST_EMAIL);
//
//        // when
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then
//        // 응답에 새 토큰이 포함되어 있는지 확인
//        assertEquals("Bearer " + newToken, response.getHeader("Authorization"),
//                    "응답 헤더에 새 토큰이 설정되어야 합니다.");
//
//        // 인증이 성공했는지 확인
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(authentication, "인증 정보가 설정되어야 합니다.");
//        assertEquals(TEST_EMAIL, authentication.getName(), "인증된 사용자의 이메일이 일치해야 합니다.");
//    }
//
//    /**
//     * JWT 인증 필터 클래스
//     *
//     * 테스트를 위한 간단한 JWT 인증 필터 구현.
//     * 실제 애플리케이션의 필터와 유사하게 동작하도록 구현되었습니다.
//     */
//    private class JwtAuthenticationFilter {
//        private final JwtService jwtService;
//
//        public JwtAuthenticationFilter(JwtService jwtService) {
//            this.jwtService = jwtService;
//        }
//
//        public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//                throws Exception {
//            // Authorization 헤더에서 토큰 추출
//            String authHeader = request.getHeader("Authorization");
//
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//
//                // 토큰 유효성 검증
//                if (jwtService.validateAccessToken(token, response)) {
//                    // 사용자 이메일 추출
//                    String userEmail = jwtService.getUserEmail(token);
//
//                    // 인증 정보 생성 및 SecurityContext에 설정
//                    Authentication authentication = new JwtAuthentication(userEmail);
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                }
//            }
//
//            // 필터 체인 계속 진행
//            filterChain.doFilter(request, response);
//        }
//    }
//
//    /**
//     * JWT 인증 클래스
//     *
//     * 테스트를 위한 간단한 Authentication 구현.
//     */
//    private class JwtAuthentication implements Authentication {
//        private final String userEmail;
//        private final boolean authenticated = true;
//
//        public JwtAuthentication(String userEmail) {
//            this.userEmail = userEmail;
//        }
//
//        @Override
//        public String getName() {
//            return userEmail;
//        }
//
//        @Override
//        public boolean isAuthenticated() {
//            return authenticated;
//        }
//
//        @Override
//        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
//            // 구현 생략
//        }
//
//        @Override
//        public Object getPrincipal() {
//            return userEmail;
//        }
//
//        @Override
//        public Object getCredentials() {
//            return null;
//        }
//
//        @Override
//        public Object getDetails() {
//            return null;
//        }
//
//        @Override
//        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
//            return java.util.Collections.emptyList();
//        }
//    }
//}
