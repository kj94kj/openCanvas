package cauCapstone.openCanvas.oauth2.oauth2.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import cauCapstone.openCanvas.oauth2.oauth2.exception.OAuth2AuthenticationProcessingException;

@RequiredArgsConstructor
@Component
public class OAuth2UserUnlinkManager {
	private final GoogleOAuth2UserUnlink googleOAuth2UserUnlink;
	
    public void unlink(OAuth2Provider provider, String accessToken) {
        if (OAuth2Provider.GOOGLE.equals(provider)) {
            googleOAuth2UserUnlink.unlink(accessToken);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Unlink with " + provider.getRegistrationId() + " is not supported");
        }
    }
}
