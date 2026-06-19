package cauCapstone.openCanvas.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

// 자체 서비스 access 토큰과 refresh 토큰을 생성한다.
// generateAccessToken 메소드의 claim, subject.. 등등에 넣을 것은 수정이 가능하다.
@Slf4j
@Component
public class JwtTokenizer {
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	@Value("${spring.jwt.secret-key}")
	private String secretKey;
	
	// base64EncodedSecretKey가 application.yml에 저장된 secretKey를 변환한 것이다.
	public String encodeBase64SecretKey() {
		return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
	}
	
	public String generateAccessToken(Map<String, Object> claims,
			  String subject,
			  Date expiration) {

		Key key = getKeyFromBase64EncodedKey(encodeBase64SecretKey());

		return Jwts.builder()
				.setClaims(claims) // 현재는 email과 role이 담기고있음.
				.setSubject(subject) 
				.setIssuedAt(Calendar.getInstance().getTime())
				.setExpiration(expiration) 
				.signWith(key)
				.compact();
	}
	
	
	// Refresh Token을 생성한다.
	// Access Token과 달리 권한 정보는 담지 않고 subject만 담는다.
	public String generateRefreshToken(String subject,
			   Date expiration) {

		Key key = getKeyFromBase64EncodedKey(encodeBase64SecretKey());

		return Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(Calendar.getInstance().getTime())
				.setExpiration(expiration)
				.signWith(key)
				.compact();
	}
	
	// Refresh Token을 생성한 뒤 Redis에 저장한다.
	// subject를 key로 사용하므로 사용자당 하나의 refresh token만 유지된다.
	public String generateAndStoreRefreshToken(String subject,
			Date expiration) {
		
		String refreshToken = generateRefreshToken(subject, expiration);
		
		RefreshToken redisToken = new RefreshToken(subject, refreshToken);
		refreshTokenRepository.save(redisToken);
		
		return refreshToken;
	}
	
	// byte화된 secretKey를 파라미터로 받고 Key 클래스의 secretKey를 리턴한다.
	private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	// JWT 서명을 검증하고 Claims를 반환한다.
	// 서명 불일치, 만료, 형식 오류 등 검증 실패 시 예외가 발생한다.
	public Claims verifySignature(String jws, String base64EncodedSecretKey) {
		
		try {
			Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(jws)
					.getBody();
			
			String subject = claims.getSubject();
			
	        log.debug("Parsed Claims: " + claims);
			return claims;
			
		} catch (JwtException e) {
	        throw new RuntimeException("유효하지 않은 JWT 토큰입니다.");
	    }
		
	}
	
	// 리프레시 토큰을 Redis에서 삭제하는 메소드
	public void deleteRefreshTokenBySubject(String subject) {
	    if (refreshTokenRepository.existsById(subject)) {
	        refreshTokenRepository.deleteById(subject);
	        log.info("Deleted refresh token for subject(email): {}", subject);
	    } else {
	        log.warn("No refresh token found for subject(email): {}", subject);
	    }
	}
	
    // 토큰들의 만료기간을 설정한다.
    public Date createAccessTokenExpiration() {
        return new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1시간
    }

    public Date createRefreshTokenExpiration() {
        return new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7); // 7일
    }
}

