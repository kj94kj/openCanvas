package cauCapstone.openCanvas.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRedisEntity;
import cauCapstone.openCanvas.websocket.chatroom.RemoveChatRoomService;
import cauCapstone.openCanvas.websocket.chatroom.RemoveEditorService;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotEntity;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotService;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

//Redis 연결 및 Pub/Sub 메시지 수신에 필요한 Bean을 설정한다.
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String config_host;

    @Value("${spring.data.redis.port}")
    private int config_port;

    @Value("${spring.data.redis.password}")
    private String config_password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(config_host); 
        config.setPort(config_port);  
        // 비밀번호가 설정된 Redis를 사용할 경우 활성화한다.
        // config.setPassword(configPassword);
        return new LettuceConnectionFactory(config);
    }
	
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    
    // 수신한 Pub/Sub 메시지를 RedisSubscriber.sendMessage()로 전달한다.
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

	    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
	    redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(ChatRoomRedisEntity.class));
		return redisTemplate;
	}
	
	@Bean
	public RedisTemplate<String, SnapshotEntity> snapshotRedisTemplate(RedisConnectionFactory connectionFactory) {
	    RedisTemplate<String, SnapshotEntity> template = new RedisTemplate<>();
	    template.setConnectionFactory(connectionFactory);
	    template.setKeySerializer(new StringRedisSerializer());
	    template.setHashKeySerializer(new StringRedisSerializer());
	    template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SnapshotEntity.class));
	    return template;
	}
	
	@Bean
	public RedisKeyExpirationListener redisKeyExpirationListener(
	        RedisMessageListenerContainer container,
            SubscribeRepository subscribeRepository,
            RemoveEditorService removeEditorService,
            SnapshotService snapshotService,
            RemoveChatRoomService removeChatRoomService) {
	    return new RedisKeyExpirationListener(container, subscribeRepository,removeEditorService, 
	    		snapshotService,removeChatRoomService);
	}
}