package cauCapstone.openCanvas.websocket.snapshot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParagraphSnapshotDto {

    private String paragraphId;
    private String body;
    private String afterParagraphId;
}