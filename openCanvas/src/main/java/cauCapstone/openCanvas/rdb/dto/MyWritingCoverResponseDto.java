package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.RoomType;

// 내가 쓴 글 목록을 보여줄때 쓰는 dto.
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

    public MyWritingCoverResponseDto(
            Long coverId,
            Long contentId,
            String title,
            String coverImageUrl,
            RoomType roomType,
            int view,
            Long likeCount,
            LocalDateTime coverTime,
            Long myWritingCount
    ) {
        this.coverId = coverId;
        this.contentId = contentId;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.roomType = roomType;
        this.view = view;
        this.likeCount = likeCount;
        this.coverTime = coverTime;
        this.myWritingCount = myWritingCount;
    }
}
