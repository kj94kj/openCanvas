package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    private final SubscribeRepository subscribeRegistryService;
    private final ChatRoomRepository chatRoomRepository;
    private final CoverRepository coverRepository;
    
    public ChatRoomRedisEntity createChatRoom(String roomId, String title, String subject, String version) {
    	
        Cover cover = coverRepository.findByTitle(title)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 제목의 Cover가 존재하지 않습니다."
                        )
                );

        if (cover.getRoomType() == RoomType.COMPLETE) {
            throw new IllegalArgumentException(
                    "작성이 완료된 문서입니다."
            );
        }
    	
        ChatRoomRedisEntity chatRoom = ChatRoomRedisEntity.create(roomId, title, subject, version);
    
    	chatRoomRepository.createRoom(chatRoom);
        
        subscribeRegistryService.registerEditorSubject(chatRoom.getRoomId(), subject);
        
        // 리스너는 채팅방마다 1개가 필요하다고 한다(사용자별 1개가 아님).
        ChannelTopic topic = new ChannelTopic(chatRoom.getRoomId());
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        
        // 문서방이 만들어지고나서 문서 편집 락을 걸기 때문에, 여기서 락을 걸지 않는다.
        
        cover.setRoomType(RoomType.EDITING);
        coverRepository.save(cover);
        
        return chatRoom;
    }
    
    public String getStringVersion(List<Integer> versionList) {
        return versionList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
    }
    
    
    public ChatRoomRedisEntity findRoomById(String roomId) {
    	return chatRoomRepository.findRoomById(roomId);
    }
}
