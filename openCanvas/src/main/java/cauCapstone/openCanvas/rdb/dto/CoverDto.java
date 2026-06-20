package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;


import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		표지 정보 응답 DTO.
		제목, 표지 이미지, 생성 시간, 조회수, 좋아요 수, 문서방 상태 정보를 포함한다.
		""")
public class CoverDto {
	@Schema(description = "coverId")
	private Long id;
	
	@Schema(description = "글 제목")
	private String title;
	
	@Schema(description = "이미지url")
	private String coverImageUrl;
	
	@Schema(description = "전체 글 관련 dto")
	private Long contentId;
	
	@Schema(description = "타임스탬프")
	private LocalDateTime time;
	
	@Schema(description = "조회수")
	private Integer view;

	@Schema(description = "좋아요갯수")
	private Long likeCount;	// TODO: 나중에 잘 안되면 (int) 해서 타입바꾸기.
	
	@Schema(description = "문서방 상태. EDITING: 편집 중, AVAILABLE: 편집 가능, COMPLETE: 완성")
	private RoomType roomType;
	
	@Schema(description = "현재 편집중인 문서방의 id")
	private String roomId;
	
	@Schema(description = "최대 이어쓸 수 있는 작가의 수")
	private Integer limit;
	
	public CoverDto(Long id, String title, String coverImageUrl, Long contentId, LocalDateTime time, Integer limit
			, RoomType roomType) {
		this.id = id;
		this.title = title;
		this.coverImageUrl = coverImageUrl;
		this.contentId = contentId;
		this.time = time;
		this.limit = limit;
		this.roomType= roomType;	}
	
	public CoverDto(Long id, String title, String coverImageUrl, LocalDateTime time, int view, Long likeCount
			, RoomType roomType, String roomId, Integer limit) {
		this.id = id;
		this.title= title;
		this.coverImageUrl = coverImageUrl;
		this.time = time;
		this.view = view;
		this.likeCount = likeCount;
		this.roomType = roomType;
		this.roomId = roomId;
		this.limit = limit;
	}
	
	public static CoverDto fromEntity(Cover cover, Long contentId) {
		
		return new CoverDto(cover.getId(), cover.getTitle(), cover.getCoverImageUrl(), contentId, 
				cover.getTime(), cover.getLimit(), cover.getRoomType());
	}
	
	
	public Cover toEntity() {
		return new Cover(title, coverImageUrl, limit);
	}
	
}
