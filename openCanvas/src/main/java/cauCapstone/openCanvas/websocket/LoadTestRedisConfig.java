package cauCapstone.openCanvas.websocket;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;

@Configuration
@Profile("loadtest")
public class LoadTestRedisConfig {

    @Bean
    public CommandLineRunner loadTestRedisListener(
            RedisMessageListenerContainer container,
            RedisSubscriber subscriber
    ) {
        return args -> {
            container.addMessageListener(
                subscriber,
                new ChannelTopic("loadtest-room1")
            );
        };
    }
}