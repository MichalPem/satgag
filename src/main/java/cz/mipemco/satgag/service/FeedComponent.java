package cz.mipemco.satgag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mipemco.satgag.dto.Article;
import cz.mipemco.satgag.dto.Meme;
import cz.mipemco.satgag.dto.MemeDto;
import cz.mipemco.satgag.jpa.ArticleRepository;
import cz.mipemco.satgag.jpa.ImageRepository;
import cz.mipemco.satgag.jpa.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;

/**
 * @author Michal Pemčák
 */
@Component
public class FeedComponent
{

	public static final Long BOT1_ID = 4L;
	public static final Long BOT2_ID = 6L;
	public static final Long BOT3_ID = 7L;
	public static final Long BOT4_ID = 8L;
	private final ArticleRepository articleRepository;
	private final ObjectMapper objectMapper;
	private final ImageRepository imageRepository;
	private final UserRepository userRepository;

	List<Article> hot = null;
	private int total;

	public FeedComponent(ArticleRepository articleRepository, ObjectMapper objectMapper, ImageRepository imageRepository,
			UserRepository userRepository)
	{
		this.articleRepository = articleRepository;
		this.objectMapper = objectMapper;
		this.imageRepository = imageRepository;
		this.userRepository = userRepository;
	}

	@Scheduled(cron = "0 0/15 * * * ?")
	public void autoPostMeme() throws IOException
	{
		List<String> items = Arrays.asList("wholesomememes","dankmemes","memes","bitcoin","cat");
		List<Long> bots = Arrays.asList(BOT1_ID,BOT2_ID,BOT3_ID,BOT4_ID);
		Random generator = new Random();
		int randomIndex = generator.nextInt(items.size());
		String url = "https://meme-api.com/gimme/"+items.get(randomIndex);

		MemeDto memeDto = objectMapper.readValue(new URL(url), MemeDto.class);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		(new URL(memeDto.url)).openStream().transferTo(baos);

		BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
		Meme meme = new Meme();

		meme.img = baos.toByteArray();
		imageRepository.saveAndFlush(meme);
		Article article = new Article();
		article.date = LocalDateTime.now();
		article.title = memeDto.title;
		article.memeId = meme.id;
		article.width = bimg.getWidth();
		article.height = bimg.getHeight();
//		article.sats = memeDto.ups;
		article.user = userRepository.findById(bots.get(generator.nextInt(bots.size())).longValue()).get();
		articleRepository.save(article);
		System.out.println(article.user.nick + " " + url);

	}

	@Scheduled(cron = "0 0 */3 * * *")
	public void prepareHotFeed()
	{

			hot = new ArrayList<>();

			List<Object[]> a = articleRepository.findGroups();

			for (Object[] e : a)
			{
				//			Integer num = ((BigInteger)e[0]).intValue();
				LocalDate date = ((Date) e[1]).toLocalDate();
				float averageSats = ((BigDecimal) e[2]).floatValue();
				List<Article> articles = articleRepository
						.findArticlesByDate(date);

				if (articles != null && !articles.isEmpty())
				{
					//					articles = articles.subList(0,articles.size()/2);
					total += articles.size();
					hot.addAll(articles);
				}
			}
	}

	public List<Article> getHotFeed(int page)
	{
		if(hot == null) {
			prepareHotFeed();
			return hot;
		}
		return page*5<hot.size()-2 ? hot.subList(page*5,min(page*5+5,hot.size()-1)) : new ArrayList<>();
	}


	@Scheduled(cron = "0 1 2 * * ?")
	@Transactional
	public void processSats()
	{
		List<Article> articles = articleRepository.findTodayArticlesFromUsers(LocalDate.now().minusDays(1));
		int totalSatsToEarn = articles.stream().mapToInt(i -> i.sats).sum();

		double totalSats = userRepository.findAllById(Arrays.asList(BOT1_ID,BOT2_ID,BOT3_ID,BOT4_ID)).stream().mapToDouble(u->u.balance).sum();

		double pricePerSat = Math.floor(totalSats/(totalSatsToEarn*1.0));

		articles.forEach(a -> userRepository.creditUser((long) (a.sats * pricePerSat),a.user.id));

		System.out.println("Total to give " + totalSatsToEarn);
		System.out.println("Price per sat " + pricePerSat);
		System.out.println("Articles: " + articles.size());
		Arrays.asList(BOT1_ID,BOT2_ID,BOT3_ID,BOT4_ID).forEach(userRepository::nullUserBalance);

	}


}
