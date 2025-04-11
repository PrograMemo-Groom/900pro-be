package programo._pro.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import programo._pro.dto.JwtUserInfoDto;

import java.security.Key;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtService와 RedisService 연동에 관한 통합테스트
 * (jwt secret는 임의의 값 할당)
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "jwt.secret=dGhpc2lzdGVzdHNlY3JldGtleWZvcmp3dHNlcnZpY2VpbnRlZ3JhdGlvbnRlc3RpdG11c3RiZWxvbmdlbm91Z2hmb3JoczI1NmFsZ29yaXRobQ==",
    "jwt.refresh_secret=dGhpc2lzdGVzdHJlZnJlc2hzZWNyZXRrZXlmb3Jqd3RzZXJ2aWNlaW50ZWdyYXRpb250ZXN0aXRtdXN0YmVsb25nZW5vdWdoZm9yaHMyNTZhbGdvcml0aG0=",
    "jwt.expiration_time=30"
})
@ActiveProfiles("test") // 테스트 프로필 활성화
class JwtRedisServiceIntegrationTest {

    // 테스트용 Base64 인코딩된 시크릿 키
    public static final String SECRET_KEY = "dGhpc2lzdGVzdHNlY3JldGtleWZvcmp3dHNlcnZpY2VpbnRlZ3JhdGlvbnRlc3RpdG11c3RiZWxvbmdlbm91Z2hmb3JoczI1NmFsZ29yaXRobQ==";
    public static final String REFRESH_SECRET_KEY = "dGhpc2lzdGVzdHJlZnJlc2hzZWNyZXRrZXlmb3Jqd3RzZXJ2aWNlaW50ZWdyYXRpb250ZXN0aXRtdXN0YmVsb25nZW5vdWdoZm9yaHMyNTZhbGdvcml0aG0=";

    @Autowired
    private RedisService redisService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh_secret}")
    private String refreshSecretKey;

    private final String testEmail = "test@example.com";
    private JwtUserInfoDto userInfoDto;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        redisTemplate.keys("*").forEach(key -> {
            redisTemplate.delete(key);
        });
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        redisTemplate.keys("*").forEach(key -> {
            redisTemplate.delete(key);
        });
    }

    @Test
    @DisplayName("JWT 토큰 생성 시 Redis에 실제로 저장되는지 통합 테스트")
    void createTokenWithRedisSaveIntegrationTest() {
        // given
        userInfoDto = new JwtUserInfoDto(testEmail);

        // when
        String accessToken = jwtService.createToken(userInfoDto);

        // then
        assertNotNull(accessToken);

        // JwtService의 saveToRedis는 accessToken을 키로, refreshToken을 값으로 저장함
        // accessToken이 Redis에 키로 존재하는지 확인
        Boolean hasKey = redisTemplate.hasKey(accessToken);
        assertTrue(hasKey, "Redis에 accessToken을 키로 가진 데이터가 존재해야 합니다");

        // Redis에서 값 조회 - getValue는 pop 방식이라 제거하지 않고 조회만 함
        Set<String> members = redisTemplate.opsForSet().members(accessToken);
        assertNotNull(members, "Redis Set이 null이 아니어야 합니다");
        assertFalse(members.isEmpty(), "Redis Set이 비어있지 않아야 합니다");
        String refreshToken = members.iterator().next();
        assertNotNull(refreshToken, "Redis에 저장된 리프레시 토큰이 존재해야 합니다");
    }

    @Test
    @DisplayName("특정 만료 시간을 가진 JWT 토큰 생성 통합 테스트")
    void createTokenWithExpirationIntegrationTest() {
        // given
        userInfoDto = new JwtUserInfoDto(testEmail);
        Instant expirationTime = Instant.now().plusSeconds(60); // 1분 후 만료

        // when
        String token = jwtService.createToken(userInfoDto, expirationTime);

        // then
        assertNotNull(token);

        // 토큰에서 이메일 추출해서 확인
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(testEmail, claims.get("email"));
    }

    @Test
    @DisplayName("액세스 토큰이 유효한 경우 통합 테스트")
    void validateAccessTokenIntegrationTest() {
        // given
        userInfoDto = new JwtUserInfoDto(testEmail);
        String accessToken = jwtService.createToken(userInfoDto);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // when
        boolean result = jwtService.validateAccessToken(accessToken, response);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("리프레시 토큰 만료 시 처리 통합 테스트")
    void expiredRefreshTokenHandlingTest() {
        // given
        userInfoDto = new JwtUserInfoDto(testEmail);
        String refreshToken = createExpiredRefreshToken();

        // 테스트용 임의 accessToken 생성 (실제 사용은 안 함)
        String fakeAccessToken = "test_access_token";

        // Redis에 만료된 리프레시 토큰 저장 - (accessToken을 키로, refreshToken을 값으로)
        redisService.add(fakeAccessToken, refreshToken);

        // when & then - 만료된 토큰이므로 유효성 검사는 실패해야 함
        try {
            boolean isValid = jwtService.validateRefreshToken(refreshToken);
            assertFalse(isValid, "만료된 토큰은 유효하지 않아야 합니다");
        } catch (Exception e) {
            // JWT 검증 실패는 예상된 결과
            assertTrue(true, "만료된 토큰은 예외를 발생시킬 수 있습니다");
        }
    }

    // 만료된 리프레시 토큰 생성 도우미 메서드
    private String createExpiredRefreshToken() {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretKey));

        // 만료된 토큰 생성
        return Jwts.builder()
                .setSubject(testEmail)
                .claim("email", testEmail)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 1000 * 60)) // 1분 전 발급
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000)) // 1초 전 만료
                .signWith(key)
                .compact();
    }
}
