package cauCapstone.openCanvas.oauth2.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cauCapstone.openCanvas.oauth2.util.CookieUtils;

import java.io.IOException;

import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

//OAuth2 인증이 실패했을 때 targetUrl(로그인 전 화면)로 리다이렉트 시킨다.
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
    	
        String targetUrl = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(("http://localhost:5173/"));	// 로그인이 실패했을 시 이 주소로 리다이렉트됨. 현재는 프론트의 첫화면으로 리턴하게되있음.

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "oauth2_authentication_failed")
                .build().toUriString();
        
        log.error("OAuth2 로그인 실패. type={}, message={}",
                exception.getClass().getName(),
                exception.getMessage());

        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}