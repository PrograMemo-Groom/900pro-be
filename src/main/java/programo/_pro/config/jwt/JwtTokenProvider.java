package programo._pro.config.jwt;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKey;

	public boolean validateToken(String token) {
		try{
			Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token);
			return true;
		}
		catch (Exception e){
			log.warn("[JWT] Invalid token: {}", e.getMessage());
			return false;
		}
	}

	public String getUserIdFromToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(secretKey.getBytes())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
}
