package cauCapstone.openCanvas.websocket.snapshot;

import java.util.List;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 문서 전체가 아닌 변경된 블록만 전송해 메시지 크기를 줄인다.
 * 새로 입장하거나 재연결한 사용자가 현재 문서 상태를 복원할 수 있도록
 * 문서방별 최신 스냅샷을 블록 단위로 Redis에 저장한다.
 *
 * key: SNAPSHOT:{roomId}
 * hashKey: block num
 * value: 해당 블록의 최신 SnapshotEntity
 */
@RequiredArgsConstructor
@Repository
public class SnapshotRepository {

    private static final String SNAPSHOT_KEY = "SNAPSHOT:";

    @Qualifier("snapshotRedisTemplate") 
    private final RedisTemplate<String, SnapshotEntity> redisTemplate;
    private HashOperations<String, String, SnapshotEntity> hashOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
    }

    public void saveSnapshot(String roomId, SnapshotEntity snapshot) {
        hashOps.put(SNAPSHOT_KEY + roomId, snapshot.getNum(), snapshot);
    }
    
    public SnapshotEntity getSnapshot(String roomId, String num) {
        return hashOps.get(SNAPSHOT_KEY + roomId, num);
    }

    public List<SnapshotEntity> getAllSnapshots(String roomId) {
        return hashOps.values(SNAPSHOT_KEY + roomId);
    }

    public void deleteSnapshot(String roomId, String num) {
        hashOps.delete(SNAPSHOT_KEY + roomId, num);
    }


    // 문서방의 모든 스냅샷 삭제
    public void deleteAllSnapshots(String roomId) {
        redisTemplate.delete(SNAPSHOT_KEY + roomId);
    }
}