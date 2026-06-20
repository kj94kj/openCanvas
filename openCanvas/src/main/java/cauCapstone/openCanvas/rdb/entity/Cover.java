package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 작품의 표지에 대한 엔티티
// 밖에서 보이는 작품 카드/표지/목록용 정보
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "covers")
public class Cover {
	@Id 
	@GeneratedValue
	private Long id;
	
	@Column(unique = true)
	private String title;
	private String coverImageUrl;
	
	// Content = 안으로 들어갔을 때의 작품 본체/상세 상태
	@OneToOne(mappedBy = "cover", cascade = CascadeType.ALL)
	private Content content;
	
	// 만든 시간
	private LocalDateTime time;
	
    @Enumerated(EnumType.STRING)
	private RoomType roomType;
	
	private String roomId;
	
	@Column(name = "cover_limit")
	private Integer limit;
	
	public Cover(String title, String coverImageUrl, Integer limit) {
		this.title = title;
		this.coverImageUrl = coverImageUrl;
		this.limit = limit;
	}
}
