//package programo._pro.service;
//
//import io.jsonwebtoken.MalformedJwtException;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import programo._pro.dto.JwtUserInfoDto;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.Date;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
///**
// * JwtService 단위 테스트
// *
// * 이 테스트 클래스는 JwtService의 핵심 기능들을 단위 테스트 방식으로 검증합니다.
// * Mockito를 사용해 외부 의존성을 격리하고, JwtService 자체의 기능만 테스트합니다.
// *
// * 테스트 내용:
// * - JWT 토큰 생성
// * - 토큰에서 사용자 이메일 추출
// * - 액세스 토큰 유효성 검증
// * - 만료 시간 확인
// * - 잘못된 형식의 토큰 처리
// * - 사용자 지정 만료 시간으로 토큰 생성
// */
//@ExtendWith(MockitoExtension.class)
//class JwtServiceUnitTest {
//
//    @Mock
//    private RedisService redisService;
//
//    @Mock
//    private HttpServletResponse response;
//
//    private JwtService jwtService;
//    private JwtUserInfoDto userInfoDto;
//    private final String testEmail = "test@example.com";
//    private final String secretKey = "ThisIsTestSecretKeyForJwtServiceUnitTestItMustBeLongEnoughForHS256Algorithm";
//    private final String refreshSecretKey = "ThisIsTestRefreshSecretKeyForJwtServiceUnitTestItMustBeLongEnoughForHS256Algorithm";
//
//    @BeforeEach
//    void setUp() {
//        // JwtService 직접 생성
//        jwtService = new JwtService(secretKey, refreshSecretKey, 30, redisService);
//        userInfoDto = new JwtUserInfoDto(testEmail);
//    }
//
//    /**
//     * JWT 토큰 생성 테스트
//     *
//     * 이 테스트는 JwtService가 유효한 JWT 토큰을 생성하는지 확인합니다.
//     * 또한 토큰이 Redis에 저장되는지 검증합니다.
//     */
//    @Test
//    @DisplayName("JWT 토큰 생성 테스트")
//    void createTokenTest() {
//        // Redis 서비스 모킹 - 이 테스트에서만 필요
//        when(redisService.add(anyString(), anyString())).thenReturn(1L);
//
//        // when
//        String token = jwtService.createToken(userInfoDto);
//
//        // then
//        assertNotNull(token);
//        assertTrue(token.length() > 0);
//
//        // 토큰이 Redis에 저장되는지 확인
//        verify(redisService, times(1)).add(anyString(), anyString());
//    }
//
//    /**
//     * JWT 토큰에서 사용자 이메일 추출 테스트
//     *
//     * 이 테스트는 생성된 JWT 토큰에서 사용자 이메일을 올바르게 추출하는지 확인합니다.
//     */
//    @Test
//    @DisplayName("JWT 토큰에서 사용자 이메일 추출 테스트")
//    void getUserEmailFromTokenTest() {
//        // given - 토큰 생성 시 레디스 서비스 호출이 필요
//        when(redisService.add(anyString(), anyString())).thenReturn(1L);
//        String token = jwtService.createToken(userInfoDto);
//
//        // when
//        String extractedEmail = jwtService.getUserEmail(token);
//
//        // then
//        assertEquals(testEmail, extractedEmail);
//    }
//
//    /**
//     * JWT 액세스 토큰 유효성 검증 테스트
//     *
//     * 이 테스트는 생성된 JWT 토큰의 유효성을 검증하는 기능이 제대로 작동하는지 확인합니다.
//     */
//    @Test
//    @DisplayName("JWT 액세스 토큰 유효성 검증 테스트")
//    void validateAccessTokenTest() {
//        // given - 토큰 생성 시 레디스 서비스 호출이 필요
//        when(redisService.add(anyString(), anyString())).thenReturn(1L);
//        String token = jwtService.createToken(userInfoDto);
//
//        // when
//        boolean isValid = jwtService.validateAccessToken(token, response);
//
//        // then
//        assertTrue(isValid);
//    }
//
//    /**
//     * JWT 만료 시간 확인 테스트
//     *
//     * 이 테스트는 JWT 토큰의 만료 시간이 현재 시간 이후로 설정되는지 확인합니다.
//     */
//    @Test
//    @DisplayName("JWT 만료 시간 확인 테스트")
//    void expiredTimeTest() {
//        // given - 토큰 생성 시 레디스 서비스 호출이 필요
//        when(redisService.add(anyString(), anyString())).thenReturn(1L);
//        String token = jwtService.createToken(userInfoDto);
//
//        // when
//        Date expiredTime = jwtService.getExpiredTime(token);
//
//        // then
//        assertNotNull(expiredTime);
//        assertTrue(expiredTime.after(new Date()));
//    }
//
//    /**
//     * 잘못된 형식의 JWT 토큰 테스트
//     *
//     * 이 테스트는 형식이 유효하지 않은 JWT 토큰을 검증할 때
//     * MalformedJwtException 예외가 발생하는지 확인합니다.
//     */
//    @Test
//    @DisplayName("잘못된 형식의 JWT 토큰 테스트")
//    void malformedTokenTest() {
//        // given
//        String invalidToken = "invalid.token.format";
//
//        // when & then
//        assertThrows(MalformedJwtException.class, () -> {
//            jwtService.validateAccessToken(invalidToken, response);
//        });
//    }
//
//    /**
//     * 만료 시간을 지정한 토큰 생성 테스트
//     *
//     * 이 테스트는 사용자가 지정한 만료 시간으로 JWT 토큰이 올바르게 생성되는지 확인합니다.
//     */
//    @Test
//    @DisplayName("만료 시간을 지정한 토큰 생성 테스트")
//    void createTokenWithCustomExpirationTest() {
//        // given
//        Instant expiredTime = Instant.now().plus(5, ChronoUnit.MINUTES);
//
//        // when
//        String token = jwtService.createToken(userInfoDto, expiredTime);
//
//        // then
//        assertNotNull(token);
//        Date tokenExpiry = jwtService.getExpiredTime(token);
//        assertEquals(expiredTime.toEpochMilli() / 1000, tokenExpiry.getTime() / 1000);
//    }
//}
