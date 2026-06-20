package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "comments")
public class Comment {
	@Id 
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "content_id")
	private Content content;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
	private List<CommentLike> commentLikes = new ArrayList<>();
	
	// 댓글 본문
	private String body;
	
	private LocalDateTime time;
	
	public Comment(Content content, User user, String body, LocalDateTime time) {
		this.content = content;
		this.user = user;
		this.body = body;
		this.time = time;
	}
}
