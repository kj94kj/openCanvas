package cauCapstone.openCanvas.websocket.chatroom;

import java.util.Set;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.websocket.chatmessage.ChatMessage;
import cauCapstone.openCanvas.websocket.chatmessage.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class RemoveEditorService {
	
	private final RedisPublisher redisPublisher;
	private final SubscribeRepository subscribeRepository;
	
    public boolean isEditor(String subject) {
    	String roomId = subscribeRepository.getRoomIdBySubject(subject);
    	
    	if(roomId != null) {
        	String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
        
        	if(subject.equals(editorSubject)) {
        		return true;
        		}
        	
        	}
    	
    	return false;
    }
	
    // 단순 disconnect상태가 아닌 문서편집자가 문서방을 나갈때 호출해야한다. 문서방에 있는 다른 유저들에게 종료하라는 메시지를 보낸다.
    public String removeEditorSubject(String subject) {
    	
    	String roomId = subscribeRepository.getRoomIdBySubject(subject);
    	
        subscribeRepository.removeSuscribe(subject); // subject -> roomId, roomId -> subject
    	
    	if(roomId != null) {
        	String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
			log.info(" editorSubject from Redis = {}", editorSubject);
        
        	if(editorSubject != null && subject.equals(editorSubject)) {
            	Set<String> subjects = subscribeRepository.getSubjectsByRoomId(roomId);
				
                sendROOMOUTmessage(subjects, roomId);
				
                subscribeRepository.removeEditorSubjectKey(roomId); // roomId -> editorSubject
				
            	subscribeRepository.removeLockKey(roomId);

            	return roomId;
        	}else {
        		log.warn("편집자가 아님: subject={}", subject);
       
        	}
    	}else {
            log.warn("subject 인증 실패. roomId없음.");

    	}
    	
    	return null;
    }
    
    public void sendROOMOUTmessage(Set<String> subjects, String roomId) {
        if (subjects == null || subjects.isEmpty()) {
            log.warn("ROOMOUT 보낼 대상 없음. roomId={}", roomId);
            return;
        }

        ChatMessage roomOutMessage = new ChatMessage();
        roomOutMessage.setType(ChatMessage.MessageType.ROOMOUT);
        roomOutMessage.setRoomId(roomId);
        roomOutMessage.setSubject(null);
        roomOutMessage.setMessage("작성자가 작성을 마쳤습니다. 종료하기 버튼을 눌러주세요.");

        redisPublisher.publish(new ChannelTopic(roomId), roomOutMessage);
    }
}
