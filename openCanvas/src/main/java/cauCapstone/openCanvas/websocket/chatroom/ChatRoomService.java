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
            throw new IllegalStateException("작성이 완료된 문서입니다.");
        }
        
        if (cover.getRoomType() == RoomType.EDITING) {
            throw new IllegalStateException("이미 작성 중인 문서입니다.");
        }
        
        boolean editorRegistered =
                subscribeRegistryService.registerEditorSubject(roomId, subject);

        if (!editorRegistered) {
            throw new IllegalStateException("이미 편집자가 존재합니다.");
        }


        try {
            ChatRoomRedisEntity chatRoom =
                    ChatRoomRedisEntity.create(roomId, title, subject, version);

            chatRoomRepository.createRoom(chatRoom);

            ChannelTopic topic = new ChannelTopic(chatRoom.getRoomId());
            redisMessageListener.addMessageListener(redisSubscriber, topic);

            cover.setRoomType(RoomType.EDITING);
            coverRepository.save(cover);

            return chatRoom;

        } catch (RuntimeException e) {
            subscribeRegistryService.removeEditorSubjectKey(roomId);
            subscribeRegistryService.removeLockKey(roomId);
            
            throw e;
        }
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
