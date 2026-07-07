package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

public interface WritingAncestorProjection {
    Integer getDepth();
    Integer getSiblingIndex();
    Integer getParentSiblingIndex();
    String getBody();
    LocalDateTime getTime();
    Long getUserId();
}