package cauCapstone.openCanvas.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;


// STOMP 엔드포인트와 메시지 브로커를 설정한다.
@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker	
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {
	
    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub");
        config.setApplicationDestinationPrefixes("/pub");
    }
    
    // 클라이언트가 연결할 STOMP 엔드포인트를 등록한다.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns(
        			"http://localhost:5173",
        			"http://43.201.95.35") 
                .withSockJS();	
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}