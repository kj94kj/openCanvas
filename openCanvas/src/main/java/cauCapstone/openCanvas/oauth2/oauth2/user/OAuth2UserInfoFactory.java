package cauCapstone.openCanvas.oauth2.oauth2.user;

import java.util.Map;

import cauCapstone.openCanvas.oauth2.oauth2.exception.OAuth2AuthenticationProcessingException;

// registrationId를 보고 Google/Naver/Kakao 중 어떤 제공자인지 판단한 뒤, 제공자별 OAuth2UserInfo 구현체로 변환함.
// Oauth2 엑세스 토큰으로 유저정보를 받은 것이다.
public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
            String accessToken,
            Map<String, Object> attributes) {
    	if (OAuth2Provider.GOOGLE.getRegistrationId().equals(registrationId)) {
    		return new GoogleOAuth2UserInfo(accessToken, attributes);
    	} else {
    		throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported");
    	}
    }
}
