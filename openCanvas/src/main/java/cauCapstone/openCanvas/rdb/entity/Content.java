package cauCapstone.openCanvas.rdb.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 캔버스 내용에 관한 엔티티. content로 칭한다.
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "contents")
public class Content {
	@Id 
	@GeneratedValue
	private Long id;
	
	private int view;
	
	@OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE)
	private List<Comment> comments = new ArrayList<>();
	
	
	@OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE)
	private List<Writing> writings = new ArrayList<>();
	
	@OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE)
	private List<Like> likes = new ArrayList<>();
	
	@OneToOne
	@JoinColumn(name = "cover_id")
	private Cover cover;
	
	@Column(unique = true)
	private String title;
	
	@Column
	private String official;
	
	@OneToMany(mappedBy = "content", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private List<ContentGenre> genres = new ArrayList<>();
	
	public Content(Cover cover) {
		this.view = 0;
		this.cover = cover;
		this.title = cover.getTitle();
		this.official = "1.1";
	}
}
