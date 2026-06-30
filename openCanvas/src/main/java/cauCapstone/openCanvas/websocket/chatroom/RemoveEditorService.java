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

        if (roomId == null) {
            log.warn("subject 인증 실패. roomId 없음. subject={}", subject);
            return null;
        }

        String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
        log.info("editorSubject from Redis = {}, requestSubject={}", editorSubject, subject);

        if (editorSubject == null || !subject.equals(editorSubject)) {
            log.warn("편집자가 아님: subject={}, editorSubject={}", subject);
            return null;
        }

        sendROOMOUTmessage(roomId);

        subscribeRepository.removeSuscribe(subject);
        subscribeRepository.removeEditorSubjectKey(roomId);
        subscribeRepository.removeLockKey(roomId);

        return roomId;
    }

    public void sendROOMOUTmessage(String roomId) {
        ChatMessage roomOutMessage = new ChatMessage();
        roomOutMessage.setType(ChatMessage.MessageType.ROOMOUT);
        roomOutMessage.setRoomId(roomId);
        roomOutMessage.setSubject(null);
        roomOutMessage.setMessage("작성자가 작성을 마쳤습니다. 종료하기 버튼을 눌러주세요.");

        log.info("ROOMOUT publish. roomId={}", roomId);

        redisPublisher.publish(new ChannelTopic(roomId), roomOutMessage);
    }
}
