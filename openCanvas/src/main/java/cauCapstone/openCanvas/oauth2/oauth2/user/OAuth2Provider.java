package cauCapstone.openCanvas.oauth2.oauth2.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//OAuth2 제공자를 식별하기 위한 enum이다.
//Spring Security의 registrationId 값과 매칭해 제공자별 사용자 정보 처리에 사용한다.
@Getter 
@RequiredArgsConstructor 
public enum OAuth2Provider {
    GOOGLE("google"),
    FACEBOOK("facebook"),
    GITHUB("github"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String registrationId;
}