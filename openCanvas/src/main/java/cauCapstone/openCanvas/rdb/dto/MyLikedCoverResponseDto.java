package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.RoomType;

public class MyLikedCoverResponseDto {
    private Long coverId;
    private Long contentId;
    private String title;
    private String coverImageUrl;
    private RoomType roomType;
    private int view;
    private Long likeCount;
    private LocalDateTime coverTime;

    public MyLikedCoverResponseDto(
            Long coverId,
            Long contentId,
            String title,
            String coverImageUrl,
            RoomType roomType,
            int view,
            Long likeCount,
            LocalDateTime coverTime
    ) {
        this.coverId = coverId;
        this.contentId = contentId;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.roomType = roomType;
        this.view = view;
        this.likeCount = likeCount;
        this.coverTime = coverTime;
    }
}
