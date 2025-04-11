package programo._pro.service;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import programo._pro.dto.JwtUserInfoDto;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

/**
 * JwtService 통합 테스트
 *
 * 이 테스트 클래스는 JwtService의 기능을 통합 테스트 방식으로 검증합니다.
 * Spring 애플리케이션 컨텍스트를 로드하여 실제 환경과 유사한 조건에서
 * JWT 토큰 관련 다양한 기능을 테스트합니다.
 *
 * 테스트 내용:
 * - JWT 토큰 생성 및 Redis 저장
 * - 토큰에서 사용자 이메일 추출
 * - 액세스 토큰 유효성 검증
 * - 만료된 토큰 처리
 * - 잘못된 형식의 토큰 처리
 * - 리프레시 토큰 유효성 검증
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.jpa.hibernate.generate-ddl=true",
    "spring.sql.init.mode=never",
    "jwt.secret=" + JwtDbServiceIntegrationTest.SECRET_KEY,
    "jwt.refresh_secret=" + JwtDbServiceIntegrationTest.REFRESH_SECRET_KEY,
    "jwt.expiration_time=30"
})
@ActiveProfiles("test") // 테스트 프로필 사용
class JwtDbServiceIntegrationTest {

    // 테스트용 Base64 인코딩된 시크릿 키
    public static final String SECRET_KEY = "dGhpc2lzdGVzdHNlY3JldGtleWZvcmp3dHNlcnZpY2VpbnRlZ3JhdGlvbnRlc3RpdG11c3RiZWxvbmdlbm91Z2hmb3JoczI1NmFsZ29yaXRobQ==";
    public static final String REFRESH_SECRET_KEY = "dGhpc2lzdGVzdHJlZnJlc2hzZWNyZXRrZXlmb3Jqd3RzZXJ2aWNlaW50ZWdyYXRpb250ZXN0aXRtdXN0YmVsb25nZW5vdWdoZm9yaHMyNTZhbGdvcml0aG0=";

    @Autowired
    private JwtService jwtService;

    @Mock
    private RedisService redisService;

    @Mock
    private HttpServletResponse response;

    private JwtUserInfoDto userInfoDto;
    private String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userInfoDto = new JwtUserInfoDto(testEmail);

        // Redis 서비스 모킹
        when(redisService.add(anyString(), anyString())).thenReturn(1L);

        // JwtService에 모의 RedisService 주입
        ReflectionTestUtils.setField(jwtService, "redisService", redisService);
    }

    /**
     * JWT 토큰 생성 테스트
     *
     * 이 테스트는 JwtService가 유효한 JWT 토큰을 생성하고
     * 해당 토큰이 Redis에 저장되는지 확인합니다.
     */
    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void createTokenTest() {
        // when
        String token = jwtService.createToken(userInfoDto);

        // then
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // 토큰이 Redis에 저장되는지 확인
        verify(redisService, times(1)).add(anyString(), anyString());
    }

    /**
     * JWT 토큰에서 사용자 이메일 추출 테스트
     *
     * 이 테스트는 생성된 JWT 토큰에서 사용자 이메일을 올바르게 추출하는지 확인합니다.
     */
    @Test
    @DisplayName("JWT 토큰에서 사용자 이메일 추출 테스트")
    void getUserEmailFromTokenTest() {
        // given
        String token = jwtService.createToken(userInfoDto);

        // when
        String extractedEmail = jwtService.getUserEmail(token);

        // then
        assertEquals(testEmail, extractedEmail);
    }

    /**
     * JWT 액세스 토큰 유효성 검증 테스트
     *
     * 이 테스트는 생성된 JWT 토큰의 유효성이 올바르게 검증되는지 확인합니다.
     */
    @Test
    @DisplayName("JWT 액세스 토큰 유효성 검증 테스트")
    void validateAccessTokenTest() {
        // given
        String token = jwtService.createToken(userInfoDto);

        // when
        boolean isValid = jwtService.validateAccessToken(token, response);

        // then
        assertTrue(isValid);
    }

    /**
     * JWT 만료 시간 확인 테스트
     *
     * 이 테스트는 JWT 토큰의 만료 시간이 현재 시간 이후로 설정되는지 확인합니다.
     */
    @Test
    @DisplayName("JWT 만료 시간 확인 테스트")
    void expiredTimeTest() {
        // given
        String token = jwtService.createToken(userInfoDto);

        // when
        Date expiredTime = jwtService.getExpiredTime(token);

        // then
        assertNotNull(expiredTime);
        assertTrue(expiredTime.after(new Date()));
    }

    /**
     * 만료된 JWT 토큰 테스트
     *
     * 이 테스트는 만료된 JWT 토큰을 검증할 때 토큰이 자동으로 갱신되는지 확인합니다.
     * 토큰 만료 시 리프레시 토큰을 사용해 새로운 토큰이 발급되고,
     * 응답 헤더에 새 토큰이 추가되는지 검증합니다.
     */
    @Test
    @DisplayName("만료된 JWT 토큰 테스트")
    void expiredTokenTest() throws Exception {
        // given
        // 1초 후 만료되는 토큰 생성
        Instant expiredTime = Instant.now().plus(1, ChronoUnit.SECONDS);
        String token = jwtService.createToken(userInfoDto, expiredTime);

        // 실제 리프레시 토큰 생성
        Method generateRefreshTokenMethod = JwtService.class.getDeclaredMethod("generateRefreshToken", JwtUserInfoDto.class);
        generateRefreshTokenMethod.setAccessible(true);
        String refreshToken = (String) generateRefreshTokenMethod.invoke(jwtService, userInfoDto);

        // 토큰 갱신을 위한 리프레시 토큰 설정
        when(redisService.getValue(token)).thenReturn(refreshToken);
        when(redisService.add(anyString(), anyString())).thenReturn(1L);

        // 2초 대기하여 토큰 만료
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("테스트 중 인터럽트 발생");
        }

        // when & then
        // 만료된 토큰은 응답 헤더에 새 토큰을 추가
        boolean result = jwtService.validateAccessToken(token, response);
        verify(response, times(1)).addHeader(eq("Authorization"), matches("Bearer .*"));
        assertTrue(result);
    }

    /**
     * 잘못된 형식의 JWT 토큰 테스트
     *
     * 이 테스트는 형식이 유효하지 않은 JWT 토큰을 검증할 때
     * MalformedJwtException 예외가 발생하는지 확인합니다.
     */
    @Test
    @DisplayName("잘못된 형식의 JWT 토큰 테스트")
    void malformedTokenTest() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.validateAccessToken(invalidToken, response);
        });
    }

    /**
     * 리프레시 토큰 유효성 검증 테스트
     *
     * 이 테스트는 생성된 리프레시 토큰의 유효성을 올바르게 검증하는지 확인합니다.
     * 리플렉션을 사용하여 private 메서드에 접근해 리프레시 토큰을 생성하고 검증합니다.
     */
    @Test
    @DisplayName("리프레시 토큰 유효성 검증 테스트")
    void validateRefreshTokenTest() throws Exception {
        // given
        // 리플렉션을 사용하여 private 메서드 접근
        Method generateRefreshTokenMethod = JwtService.class.getDeclaredMethod("generateRefreshToken", JwtUserInfoDto.class);
        generateRefreshTokenMethod.setAccessible(true);

        String refreshToken = (String) generateRefreshTokenMethod.invoke(jwtService, userInfoDto);

        // when & then
        assertTrue(jwtService.validateRefreshToken(refreshToken));
    }
}
