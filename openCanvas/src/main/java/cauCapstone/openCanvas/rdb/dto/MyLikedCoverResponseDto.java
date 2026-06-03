package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyLikedCoverResponseDto {
    private Long coverId;
    private Long contentId;
    private String title;
    private String coverImageUrl;
    private RoomType roomType;
    private int view;
    private Long likeCount;
    private LocalDateTime coverTime;

}
