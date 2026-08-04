package cauCapstone.openCanvas.websocket.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SnapshotRepository {

    private static final String SNAPSHOT_KEY = "SNAPSHOT:";
    private static final String SNAPSHOT_ORDER_KEY = "SNAPSHOT_ORDER:";

    @Qualifier("snapshotRedisTemplate")
    private final RedisTemplate<String, SnapshotEntity> snapshotRedisTemplate;

    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate;

    private HashOperations<String, String, SnapshotEntity> hashOps;
    private ListOperations<String, String> orderListOps;

    @PostConstruct
    private void init() {
        hashOps = snapshotRedisTemplate.opsForHash();
        orderListOps = stringRedisTemplate.opsForList();
    }

    private String snapshotKey(String roomId) {
        return SNAPSHOT_KEY + roomId;
    }

    private String orderKey(String roomId) {
        return SNAPSHOT_ORDER_KEY + roomId;
    }

    // afterparagraphId가 오지 않은경우는 기존에 있던 문단임.
    // afterparagraphId가 온 경우는 새로운 문단인데, 맨 뒤에 쓰는 경우 중간에 새 문단을 쓰는 경우가 갈림.
    public void saveSnapshot(String roomId, SnapshotEntity snapshot, String afterParagraphId) {
        String snapshotKey = snapshotKey(roomId);
        String orderKey = orderKey(roomId);

        String paragraphId = snapshot.getParagraphId();

        Boolean exists = hashOps.hasKey(snapshotKey, paragraphId);

        hashOps.put(snapshotKey, paragraphId, snapshot);

        if (Boolean.TRUE.equals(exists)) {
            return;
        }

        if (afterParagraphId == null || afterParagraphId.isBlank()) {
            orderListOps.rightPush(orderKey, paragraphId);
            return;
        }

        String lastParagraphId = orderListOps.index(orderKey, -1);

        if (Objects.equals(lastParagraphId, afterParagraphId)) {
            orderListOps.rightPush(orderKey, paragraphId);
            return;
        }

        Long result = orderListOps.rightPush(orderKey, afterParagraphId, paragraphId);

        if (result == null || result == -1) {
            orderListOps.rightPush(orderKey, paragraphId);
        }
    }
    
    public List<SnapshotEntity> findSnapshots(String roomId) {
        String snapshotKey = snapshotKey(roomId);
        String orderKey = orderKey(roomId);

        List<String> paragraphIds = orderListOps.range(orderKey, 0, -1);

        if (paragraphIds == null || paragraphIds.isEmpty()) {
            return List.of();
        }

        List<SnapshotEntity> snapshots =
                hashOps.multiGet(snapshotKey, paragraphIds);

        if (snapshots == null) {
            return List.of();
        }

        return snapshots.stream()
                .filter(Objects::nonNull)
                .toList();
    }
    
    public void deleteSnapshots(String roomId) {
        snapshotRedisTemplate.delete(snapshotKey(roomId));
        stringRedisTemplate.delete(orderKey(roomId));
    }
}