	package cauCapstone.openCanvas.oauth2.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cauCapstone.openCanvas.oauth2.oauth2.service.OAuth2UserPrincipal;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2Provider;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2UserUnlinkManager;
import cauCapstone.openCanvas.oauth2.util.CookieUtils;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;
import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

//OAuth2 인증 성공 후 로그인 또는 연동 해제(unlink) 흐름을 처리한다.
//로그인 성공 시 자체 JWT를 발급하고, unlink 모드에서는 사용자 정보와 Refresh Token을 삭제한 뒤 제공자 연동을 해제한다.
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;
    private final JwtTokenizer jwtTokenizer;

    // determineTargetUrl 메소드로 로그인을 하는건지 회원탈퇴를 하는건지 판단한 뒤 targetUrl로 리다이렉트 한다.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String targetUrl;

        targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
        	log.debug("응답이 이미 커밋되어 리다이렉트할 수 없습니다.");
            return;
        }

        clearAuthenticationAttributes(request, response);	
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // login 모드인지, unlink 모드인지 판단하고 targetUrl을 리턴한다.
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

    	// 쿠키에서 redirectUri와 mode를 꺼낸다.
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("");

        // OAuth2UserPrincipal을 가져온다.
        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            log.warn("OAuth2 principal 변환 실패. principalType={}",
                    authentication.getPrincipal().getClass().getName());
        	
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "Login failed")
                    .build().toUriString();
        }

        if ("login".equalsIgnoreCase(mode)) {
        	String email = principal.getUsername(); // email을 username으로 사용한다.
        	
            // 최초 로그인 사용자는 User 엔티티를 생성해 저장한다.
            // 별도의 OAuth2User 엔티티는 두지 않고 User.email을 기준으로 사용자를 식별한다.
            Optional<User> optionalUser = userRepository.findByEmail(email);
            User user;

            if (optionalUser.isPresent()) { 
                user = optionalUser.get();
            } else { 
                User newUser = new User(email, email, Role.USER);
                user = userRepository.save(newUser);
                // log.info("OAuth2 신규 사용자 생성. email={}", email);
                
                // 추천서버 요청 (유저 생성)
                // recommendService.createUser(user.getId());
            }
        	
            // OAuth2 인증 성공 후 서비스 자체 JWT Access Token을 발급한다.
        	Map<String, Object> claims = Map.of(
        		    "email", principal.getUserInfo().getEmail(),
        		    "role", user.getRole().name()
        		);

        		String accessToken = jwtTokenizer.generateAccessToken(
        		    claims,
        		    principal.getUserInfo().getEmail(),
        		    new Date(System.currentTimeMillis() + 1000 * 60 * 60) // 60분짜리 Access Token
        		);
        	
        		String refreshToken = jwtTokenizer.generateAndStoreRefreshToken(
        			principal.getUserInfo().getEmail(),
        			new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7) // 7일짜리 Refresh Token
        		);
        		
        		
                log.debug("OAuth2 로그인 성공 - JWT 발급 완료. email={}", email);

                // targetUrl에 엑세스 토큰과 리프레시 토큰의 쿼리 패러미터를 붙여서 전송한다.
                return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("access_token", accessToken)
                    .queryParam("refresh_token", refreshToken)
                    .build().toUriString();

        } else if ("unlink".equalsIgnoreCase(mode)) {

            String accessToken = principal.getUserInfo().getAccessToken();
            OAuth2Provider provider = principal.getUserInfo().getProvider();

            // 유저정보를 DB에서 삭제 한다.
        	String email = principal.getUsername();	// 이게 이메일
        	
        	User user = userRepository.findByEmail(email)
        		    .orElseGet(() -> {
        		    	throw new IllegalArgumentException("회원탈퇴할 유저를 찾을 수 없습니다.");
        		    });
            
        	userRepository.delete(user); // 삭제
            
            // 리프레시 토큰을 삭제 한다.
        	jwtTokenizer.deleteRefreshTokenBySubject(email);
        	
            // 연동 끊기(회원탈퇴)는 OAuth2UserUnlinkManager가 담당한다.
            oAuth2UserUnlinkManager.unlink(provider, accessToken);

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .build().toUriString();
        }
        
        log.warn("지원하지 않는 OAuth2 mode 요청. mode={}", mode);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "Login failed")
                .build().toUriString();
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        }
        return null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
