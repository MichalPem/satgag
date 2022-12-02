package cz.mipemco.satgag.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Pemčák
 */
@Entity
public class Article
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public LocalDateTime date;
	@Column(name = "meme_id", nullable = true)
	public Long memeId;
	@Column(name = "parent_id", nullable = true)
	public Long parentId;
	@OneToOne
	public User user;
	public String title;
	public Integer sats = 0;

	public Article()
	{
	}

	public Article(Article other)
	{
		this.id = other.id;
		this.date = other.date;
		this.memeId = other.memeId;
		this.parentId = other.parentId;
		this.user = other.user;
		this.title = other.title;
		this.sats = other.sats;
	}
}
