package programo._pro.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import programo._pro.dto.JwtUserInfoDto;
import programo._pro.global.exception.NotFoundUserException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
@Slf4j
public class JwtService {
    private final Key key;
    private final Key refreshKey;
    private final RedisService redisService;
    private final long accessTokenExpireTime;
    private final long refreshTokenExpireTime;

    private static final long ONE_SECOND = 1000;
    private static final long ONE_MINUTE = ONE_SECOND * 60;
    private static final String EMAIL_KEY = "email";
    private static final String INVALID_TOKEN_MESSAGE = "INVALID_TOKEN";

    // 여기서 Base64는 충분히 안전합니다. 그 이유는:
    // 1. Base64는 단순히 인코딩 방식일 뿐, 암호화 방식이 아닙니다.
    // 2. JWT의 실제 보안은 서명(signature)에 의존하며, 이는 HMAC-SHA256과 같은 암호화 알고리즘을 사용합니다.
    // 3. Base64는 단순히 바이너리 데이터를 텍스트로 변환하는 용도로만 사용됩니다.
    // 4. JWT의 구조는 Header.Payload.Signature 형식이며, 각 부분은 Base64로 인코딩됩니다.
    // 5. 실제 보안은 JWT_SECRET 키의 관리와 서명 알고리즘에 의존합니다.
    public JwtService(@Value("${jwt.secret}") String secretKey,
                      @Value("${jwt.refresh_secret}") String refreshSecretKey,
                      @Value("${jwt.expiration_time}") long accessTokenExpiresTime,
                      RedisService redisService) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretKey));
        this.redisService = redisService;
        accessTokenExpireTime = ONE_MINUTE * accessTokenExpiresTime;        // 12 hours (720 min)
        refreshTokenExpireTime = ONE_MINUTE * (accessTokenExpiresTime * 2); // 24 hours (1440 min)
    }

    public String createToken(JwtUserInfoDto member) {
        String accessToken = generateAccessToken(member);
        String refreshToken = generateRefreshToken(member);
        saveToRedis(accessToken, refreshToken);
        return accessToken;
    }

    public String createToken(JwtUserInfoDto member, Instant expiredTime) {
        Claims claims = Jwts.claims();
        claims.put(EMAIL_KEY, member.getEmail());
        Date expires = Date.from(expiredTime);

        return makeToken(key, claims, expires);
    }

    private String generateAccessToken(Claims claims) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expires = now.plusSeconds(accessTokenExpireTime); // CurrentTime + ExpireTime

        return makeToken(key, claims, Date.from(now.toInstant()), Date.from(expires.toInstant()));
    }

    private String generateAccessToken(JwtUserInfoDto member) {
        Claims claims = Jwts.claims();
        claims.put(EMAIL_KEY, member.getEmail());

        long now = (new Date()).getTime();
        Date expires = new Date(now + accessTokenExpireTime);

        return makeToken(key, claims, expires);
    }

    private String generateRefreshToken(JwtUserInfoDto member) {
        Claims claims = Jwts.claims();
        claims.put(EMAIL_KEY, member.getEmail());
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expires = now.plusSeconds(refreshTokenExpireTime);
        return makeToken(refreshKey, claims, Date.from(now.toInstant()), Date.from(expires.toInstant()));
    }

    private String makeToken(Key secretKey, Claims claims, Date expires) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expires)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String makeToken(Key secretKey, Claims claims, Date start, Date expires) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expires)
                .setIssuedAt(start)
                .setExpiration(expires)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserEmail() {
        return getUserInfo(EMAIL_KEY);
    }

    public String getUserEmail(String accessToken) {
        return parseClaims(accessToken)
                .get(EMAIL_KEY, String.class);
    }

    private String getUserInfo(String needKey) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
            return switch (needKey) {
                // UserDetails 인터페이스에서는 getUsername()이 실제로 이메일을 반환함
                case EMAIL_KEY -> userDetails.getUsername();
                default -> throw new NotFoundUserException();
            };
        }
        return null;
    }

    public Date getExpiredTime(String token) {
        return parseClaims(token).getExpiration();
    }


    /**
     * JWT Claims 추출
     *
     * @return JWT Claims
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null, e.getClaims(), "Expired JWT Token");
        }
    }

    private Claims parseClaimsForRefresh(String refreshToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(refreshToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private void saveToRedis(String accessToken, String refreshToken) {
        redisService.add(accessToken, refreshToken);
    }


    private String renewToken(String accessToken) {
        String refreshToken = redisService.getValue(accessToken);
        boolean isValid = validateRefreshToken(refreshToken);
        if (isValid) return generateAccessToken(parseClaimsForRefresh(refreshToken));
        throw new ExpiredJwtException(null, null, null, null);
    }

    public boolean validateAccessToken(String token, HttpServletResponse response) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info(INVALID_TOKEN_MESSAGE, e);
            throw new MalformedJwtException(INVALID_TOKEN_MESSAGE);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            response.addHeader("Authorization", "Bearer " + renewToken(token));
            return true;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info(INVALID_TOKEN_MESSAGE + " ::: Refresh", e);
            throw new MalformedJwtException(INVALID_TOKEN_MESSAGE + " ::: Refresh");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            throw new MalformedJwtException("Expired Refresh Token.\n Please login again.\n");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

}
