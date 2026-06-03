package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 내가 쓴 글 목록을 보여줄때 쓰는 dto.
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyWritingCoverResponseDto {
    private Long coverId;
    private Long contentId;
    private String title;
    private String coverImageUrl;
    private RoomType roomType;
    private int view;
    private Long likeCount;
    private LocalDateTime coverTime;
    private Long myWritingCount;

}
