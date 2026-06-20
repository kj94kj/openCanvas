package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Content안의 글. 이어쓰기 단위로 구분되는 글 조각. 
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "writings")
public class Writing {
	@Id
	@GeneratedValue
	private Long id;
	
	private int depth; 
	private int siblingIndex; 
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Writing parent;
	
	@Column(columnDefinition = "TEXT")
	private String body;
	private LocalDateTime time;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id")
	private Content content;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	public Writing(int depth, int siblingIndex, Writing parent, String body, LocalDateTime time, Content content, User user) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parent = parent;
		this.body = body;
		this.time = time;
		this.content = content;
		this.user = user;
	}
}
