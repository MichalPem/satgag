package cz.mipemco.satgag.jpa;

import cz.mipemco.satgag.dto.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Michal Pemčák
 */
public interface ArticleRepository extends PagingAndSortingRepository<Article,Long>
{

	Article findArticleByMemeId(Long meme_id);

	@Query(value = "select * from article where date(`date`) = :date and sats > :sats and parent_id is null",nativeQuery = true)
	List<Article> findArticlesByDateAfterOrderBySatsDesc(LocalDate date,Integer sats);

	@Query(value = "SELECT count(id) as num, DATE(date) as dates, avg(sats) as averages FROM `article` GROUP BY DATE(article.date) order by dates desc",nativeQuery = true)
	List<Object[]> findGroups();

	@Query(value = "select * from article where date(date) = :date and `sats` > 0  and `parent_id` is null order by `sats` desc",nativeQuery = true)
	List<Article> findArticlesByDate(LocalDate date);

	@Query(value = "select * from article where `user_id` not in ('4','6','7','8') and date(`date`) = '2022-12-01'",nativeQuery = true)
	List<Article> findTodayArticlesFromUsers(LocalDate date);

	@Query(value = "select * from article where `user_id` in ('4','6','7','8') and date(`date`) = '2022-12-01'",nativeQuery = true)
	List<Article> findTodayArticlesFromBots(LocalDate date);

	Page<Article> findArticleByParentIdOrderByDateDesc(Long id,PageRequest pageRequest);

	Page<Article> findArticlesByParentIdIsNull(PageRequest pageRequest);
}
