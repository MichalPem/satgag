package cz.mipemco.satgag.rest;

import cz.mipemco.satgag.dto.Article;
import cz.mipemco.satgag.dto.Meme;
import cz.mipemco.satgag.dto.User;
import cz.mipemco.satgag.jpa.ArticleRepository;
import cz.mipemco.satgag.jpa.ImageRepository;
import cz.mipemco.satgag.jpa.UserRepository;
import cz.mipemco.satgag.ln.LnManager;
import cz.mipemco.satgag.service.FeedComponent;
import cz.mipemco.satgag.util.Blake3;
import cz.mipemco.satgag.util.Hash;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static cz.mipemco.satgag.util.Hash.getDbKey;
import static java.lang.Math.min;

/**
 * @author Michal Pemčák
 */
@org.springframework.web.bind.annotation.RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api")
public class RestController
{

	private final ArticleRepository articleRepository;
	private final ImageRepository imageRepository;
	private final UserRepository userRepository;
	private final FeedComponent feedComponent;

	private final LnManager lnManager;

	public RestController(ArticleRepository imageRepository, ImageRepository imageRepository1, UserRepository userRepository,
			FeedComponent feedComponent, LnManager lnManager)
	{
		this.articleRepository = imageRepository;
		this.imageRepository = imageRepository1;
		this.userRepository = userRepository;
		this.feedComponent = feedComponent;
		this.lnManager = lnManager;
	}


	@GetMapping({"/feed/{feed}","/feed/"})
	public List<Article> getImages(@PathVariable(required = false,value = "feed") String feed,@RequestParam(name = "page",required = false) Integer page)
	{
		if(feed != null && feed.equals("new"))
		{
			return articleRepository.findArticlesByParentIdIsNull(PageRequest.of(page,5).withSort(Sort.Direction.DESC,"date")).getContent().stream()
					.peek(article -> {
						article.sats = article.sats == null ? 0 : article.sats;
						article.user.balance = null;
						article.user.password = null;
					})
					.collect(Collectors.toList());
		}
		if(feed != null && !feed.equals("null"))
		{
			return  articleRepository.findArticleByParentIdOrderByDateDesc(Long.valueOf(feed),PageRequest.of(page,5)).getContent().stream()
					.peek(article -> {
						article.sats = article.sats == null ? 0 : article.sats;
						article.user.balance = null;
						article.user.password = null;
					})
					.collect(Collectors.toList());
		}


		return feedComponent.getHotFeed(page).stream().peek(article -> {
			article.sats = article.sats == null ? 0 : article.sats;
			article.user.balance = null;
			article.user.password = null;
		}).collect(Collectors.toList());
	}

	@GetMapping("/a/{id}")
	public ResponseEntity<Article> getArticle(@PathVariable Long id) throws FileNotFoundException
	{

		return ResponseEntity.ok(loadArticle(id));
	}

	@Transactional
	public Article loadArticle(Long id) throws FileNotFoundException
	{
		Optional<Article> a = articleRepository.findById(id);
		if(a.isPresent())
		{
			Article o = a.get();
			o.user.balance = null;
			o.user.password = null;
			return o;
		}
		throw new FileNotFoundException();
	}

	@GetMapping("/img/{id}")
	public @ResponseBody ResponseEntity<InputStreamResource> getImage(@PathVariable Long id)
	{
		Optional<Meme> m = imageRepository.findById(id);
		return m.map(meme -> ResponseEntity.ok().contentLength(meme.img.length).contentType(MediaType.IMAGE_JPEG)
				.body(new InputStreamResource(new ByteArrayInputStream(meme.img)))).orElseGet(() -> ResponseEntity.notFound().build());

	}

	@PatchMapping("/usr/{id}")
	@Transactional
	public @ResponseBody ResponseEntity<?> getImage(@PathVariable String id,
			@RequestParam(name = "imgurl",required = false) String imgurl,
			@RequestParam(name = "nick",required = false) String nick)
	{
		return userRepository.findUserByPassword(getDbKey(id)).map(u -> {

			u.imgurl = imgurl;

			if(!userRepository.existsByLnid(nick))
			{
				u.nick = nick;
				u.lnid = nick;
			} else {
				int c = 1;
				while (userRepository.existsByLnid(nick+c)||c<1000) c++;
				if(c==1000) return ResponseEntity.badRequest().body("Nick already taken");
				u.lnid = nick+c;
				u.nick = nick+c;
			}
			u.password = getDbKey(id);
			userRepository.saveAndFlush(u);

			return ResponseEntity.ok().body(new User(u.id,id,u.imgurl,u.nick,u.balance,u.lnid));
		}).orElseGet(()->ResponseEntity.notFound().build());
	}

	@PatchMapping("/pay/{id}")
	public @ResponseBody ResponseEntity<?> getImage(@PathVariable String id,
			@RequestParam(name = "to",required = false) Integer to,
			@RequestParam(name = "article_id",required = false) Integer article_id,
			@RequestParam(name = "amount",required = false) Integer amount)
	{
		return userRepository.findUserByPassword(getDbKey(id)).map(u -> {
			BigInteger r;
			if(u.balance < amount) return  ResponseEntity.badRequest().body(new InsufficientMoneyException( Coin.valueOf(amount-u.balance)).getMessage());
			Optional<Article> a = articleRepository.findById(article_id.longValue());
			if(!a.isPresent()) return ResponseEntity.badRequest().build();
			synchronized (RestController.class)
			{
				userRepository.updateBalance(getDbKey(id), !Objects.equals(a.get().user.id, u.id) ? to :  FeedComponent.BOT1_ID, article_id,amount);
				r = userRepository.verify();
			}
			if(r.intValue() == 1 )
			{


				a.get().sats += amount;
				Article dto = new Article(a.get());
				dto.user = new User(u.id,id,u.imgurl,u.nick,u.balance,u.lnid);
				return ResponseEntity.ok(dto);
			}
			return ResponseEntity.badRequest().build();
		}).orElseGet(()->ResponseEntity.notFound().build());
	}

	@PatchMapping("/withdrawal/{id}")
	public @ResponseBody ResponseEntity<?> withdrawal(@PathVariable String id,
			@RequestParam(name = "lna") String lna,
			@RequestParam(name = "amount") Integer amount)
	{
		return userRepository.findUserByPassword(getDbKey(id)).map(u -> {
			//process payment
			try
			{
				//TODO calculate fee
				if(u.balance < amount) return  ResponseEntity.badRequest().body(new InsufficientMoneyException( Coin.valueOf(amount-u.balance)).getMessage());
				lnManager.pay(u,lna,amount);
			} catch (Exception e)
			{
				e.printStackTrace();
				return ResponseEntity.internalServerError().body(e.getMessage());
			}
			return ResponseEntity.ok().body(new User(u.id,id,u.imgurl,u.nick,u.balance,u.lnid));
		}).orElseGet(()->ResponseEntity.notFound().build());
	}

	@GetMapping("/login/{key}")
	public ResponseEntity<User> register(@PathVariable String key)
	{

		String dbkey = getDbKey(key);
		return userRepository.findUserByPassword(dbkey).map(u -> ResponseEntity.ok().body(new User(u.id,key,u.imgurl,u.nick,u.balance,u.lnid))).orElseGet(() -> {
			Blake3 hasher = Blake3.newInstance();
			hasher.update(key.getBytes());
			String hexhash = hasher.hexdigest(4);
			User user = new User();
			user.password = dbkey;
			user.lnid = Base58.encode(hexhash.getBytes());
			userRepository.saveAndFlush(user);
			return ResponseEntity.ok().body(new User(user.id,key,user.imgurl,user.nick,user.balance,user.lnid));
		});

	}

	@RequestMapping(value = "/save/{key}", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<?> handleFileUpload(
			@PathVariable String key,
			@RequestParam(value = "title",required = false) String title,
			@RequestParam(value = "parent",required = false,defaultValue = "null") Long parent,
			@RequestParam(value = "file", required = false) MultipartFile file) {
		return userRepository.findUserByPassword(getDbKey(key)).map(u -> {

			try
			{
				if(u.balance < 10) return  ResponseEntity.badRequest().body(new InsufficientMoneyException( Coin.valueOf(10)).getMessage());
				userRepository.debitUser(10L,u.id);
				Article article = new Article();

				if(file != null)
				{
					Meme meme = new Meme();
					meme.img = file.getBytes();
					imageRepository.saveAndFlush(meme);
					article.memeId = meme.id;
				}


				article.title = title;
				article.user = u;
				article.parentId = parent == -1 ? null : parent;
				article.date = LocalDateTime.now();

				articleRepository.save(article);

				Article dto = new Article(article);
				dto.user = new User(u.id,key,u.imgurl,u.nick,u.balance,u.lnid);
				return ResponseEntity.ok(dto);
			} catch (IOException e)
			{
				e.printStackTrace();
				return ResponseEntity.internalServerError().body(e.getMessage());
			}



		}).orElseGet(()->ResponseEntity.notFound().build());
	}


}
