package cauCapstone.openCanvas.jwt;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 604800) // Redis 저장 TTL: 7일
public class RefreshToken {
	
	// subject은 email과 동일함.
	@Id
	private String email;
	
	private String token;
}
