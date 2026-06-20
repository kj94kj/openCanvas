package cauCapstone.openCanvas.websocket.snapshot;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnapshotEntity implements Serializable {
    private static final long serialVersionUID = 6494678977089006630L;
    
    private String roomId;
    private String body;
    private String num;
    private long time;
    
    public static SnapshotEntity makeSnapshot(String roomId, String body, String num, long time) {
        SnapshotEntity snapshot = new SnapshotEntity();
        snapshot.setRoomId(roomId);
        snapshot.setBody(body);
        snapshot.setNum(num);
        snapshot.setTime(time);
        return snapshot;
    }
}
