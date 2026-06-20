package cauCapstone.openCanvas.websocket.chatroom;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RemoveChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    private final CoverRepository coverRepository;
	
    // 문서방은 두가지 조건에서 삭제된다. 1. 편집자가 작성 종료 버튼을 누를 때, 2. 편집자가 연결이 끊기고 3분동안 재연결이 없을 때(백)
    public void removeChatRoom(String roomId) {
    	
    	if(roomId != null) {
        	
            chatRoomRepository.deleteRoom(roomId);

            ChannelTopic topic = new ChannelTopic(roomId);
            redisMessageListener.removeMessageListener(redisSubscriber, topic);	
            
            Cover cover = coverRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제목의 Cover가 존재하지 않습니다."));

            if(cover.getRoomType() != RoomType.COMPLETE) {
                cover.setRoomType(RoomType.AVAILABLE); 
            }
            coverRepository.save(cover);
    	}
    }
}