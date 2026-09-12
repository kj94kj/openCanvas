package cauCapstone.openCanvas.websocket.chatmessage;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Profile("loadtest")
@Controller
@RequiredArgsConstructor
public class LoadTestController {

    private final RedisPublisher redisPublisher;

    @MessageMapping("/load-test")
    public void loadTest(ChatMessage message) {

        redisPublisher.publish(
            new ChannelTopic(message.getRoomId()),
            message
        );
    }
}