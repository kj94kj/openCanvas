package cauCapstone.openCanvas.oauth2.oauth2.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2UserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

//OAuth2 인증 후 Spring Security가 사용할 사용자 Principal 객체이다.
//OAuth2UserInfo를 감싸 OAuth2User와 UserDetails 인터페이스에 맞게 사용자 정보를 제공한다.
public class OAuth2UserPrincipal implements OAuth2User, UserDetails{
	
    private final OAuth2UserInfo userInfo;

    public OAuth2UserPrincipal(OAuth2UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userInfo.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return userInfo.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return userInfo.getEmail();
    }

    public OAuth2UserInfo getUserInfo() {
        return userInfo;
    }
}
