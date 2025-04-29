package programo._pro.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKey;

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(Base64.getDecoder().decode(secretKey)) // ✅ Base64 decode
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			log.warn("[JWT] Invalid token: {}", e.getMessage());
			return false;
		}
	}

	public String getUserIdFromToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(Base64.getDecoder().decode(secretKey)) // ✅ 동일하게 decode
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	// 🔥 여기 추가
	public Long getUserId(String token) {
		Claims claims = parseClaims(token);
//		return Long.parseLong(claims.getSubject()); // subject를 userId로 쓰는 경우
		return claims.get("userId", Long.class);  // ✅ subject가 아니라, "userId" 필드
	}
	/**
	 * JWT Claims 추출
	 *
	 * @return JWT Claims
	 */
	// ✨ 새로운 parseClaims
	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(Base64.getDecoder().decode(secretKey))
				.build()
				.parseClaimsJws(token)
				.getBody();
	}



}
