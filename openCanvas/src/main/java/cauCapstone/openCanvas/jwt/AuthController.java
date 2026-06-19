package cauCapstone.openCanvas.jwt;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "엑세스 토큰 재발급 API", description = """
		 [JWT 토큰 재발급 가이드]
 1. Access Token이 만료되었을 때

    서버에 요청을 보낼 때 Authorization: Bearer {accessToken} 헤더가 필요한데,
    이 토큰이 만료되면 401 Unauthorized 응답이 옵니다.

 2. Refresh API로 Access Token 재발급 받기

    만료된 Access Token 대신 저장해둔 Refresh Token을 사용해
    새로운 Access Token을 발급받을 수 있습니다.
		""")
public class AuthController {

    private final JwtTokenizer jwtTokenizer;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    @Operation(
            summary = "엑세스 토큰 재발급",
            description = "리프레시 토큰을 이용해 새로운 엑세스 토큰을 발급받습니다.")
            @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "새로운 엑세스 토큰 발급 성공"),
                @ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 리프레시 토큰 누락"),
                @ApiResponse(responseCode = "401", description = "리프레시 토큰이 없거나 유효하지 않음")
            }
        )
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        if (body == null || body.get("refreshToken") == null || body.get("refreshToken").isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("리프레시 토큰이 필요합니다.");
        }
    	
        String refreshToken = body.get("refreshToken");

        try {
            // 1. 서명 검증
            Claims claims = jwtTokenizer.verifySignature(refreshToken, jwtTokenizer.encodeBase64SecretKey());
            String email = claims.getSubject();

            // 2. Redis에서 저장된 리프레시 토큰 검증
            RefreshToken savedToken = jwtTokenizer.refreshTokenRepository.findById(email)
                    .orElse(null);

            if (savedToken == null) {
                log.warn("Refresh token not found in Redis. email={}", email);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 리프레시 토큰입니다.");
            }

            if (!savedToken.getToken().equals(refreshToken)) {
                log.warn("리프레시 토큰 불일치. email={}", email);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 리프레시 토큰입니다.");
            }


            // 3. 유저 DB에서 권한 조회
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                log.error("Refresh token subject exists but user not found. email={}", email);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 리프레시 토큰입니다.");
            }

            // 4. 새 엑세스 토큰 발급
            Map<String, Object> newClaims = Map.of("email", user.getEmail(),
            	    					"role", user.getRole().name());
            Date newExpiration = jwtTokenizer.createAccessTokenExpiration();

            String newAccessToken = jwtTokenizer.generateAccessToken(newClaims, user.getEmail(), newExpiration);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {
            log.warn("리프레시 토큰 검증 실패. reason={}", e.getMessage());
        	
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
