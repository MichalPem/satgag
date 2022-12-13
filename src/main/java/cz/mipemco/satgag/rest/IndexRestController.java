package cz.mipemco.satgag.rest;

import cz.mipemco.satgag.dto.Article;
import cz.mipemco.satgag.jpa.ArticleRepository;
import org.bitcoinj.core.Base58;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Michal Pemčák
 */
@org.springframework.web.bind.annotation.RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "**")
public class IndexRestController
{

	private final ArticleRepository articleRepository;
	@Value("${backend}")
	public String backend;


	public IndexRestController(ArticleRepository articleRepository)
	{
		this.articleRepository = articleRepository;
	}

	@GetMapping()
	public @ResponseBody Object index(HttpServletRequest request,@RequestHeader(value = "User-Agent") String userAgent) throws IOException
	{
		String pathInfo = request.getServletPath();


		Resource r = new ClassPathResource("static"+pathInfo);
		if(r.exists() && !pathInfo.equals("/"))
		{
			//			System.out.println(">"+pathInfo );
			Path path = new File(pathInfo).toPath();
			String mimeType = Files.probeContentType(path);
			final HttpHeaders httpHeaders = new HttpHeaders();

			if (mimeType != null && !mimeType.isEmpty()) {
				httpHeaders.setContentType(MediaType.parseMediaType(mimeType));
			} else {
				httpHeaders.setContentType(MediaType.TEXT_HTML);
			}
			return new ResponseEntity<>(r.getInputStream().readAllBytes(), httpHeaders, HttpStatus.OK);

		}
		ClassPathResource cr =  new ClassPathResource("static/index.html");
		String s = new String(cr.getInputStream().readAllBytes());

		if(pathInfo.contains("/a/"))
		{
			pathInfo = pathInfo.replaceAll("/a/","");
			byte[] b = Base58.decode(pathInfo);
			long id = 0;
			for (int i = b.length - 1; i >= 0; i--) {
				id = (id * 256) + (b[i] & 0xff);
			}
			Optional<Article> a = articleRepository.findById(id);
			if(a.isPresent())
			{
				s = s.replaceAll("!!! !!!width",a.get().width+"");
				s = s.replaceAll("!!! !!!height",a.get().height+"");
				s = s.replaceAll("!!! !!!imgUrl","https://"+backend+"/api/img/"+a.get().memeId);
				s = s.replaceAll("!!! !!!title","SatGag");
				s = s.replaceAll("!!! !!!description",a.get().title);

			}
			s = s.replaceAll("!!! !!!title","SatGag");
			s = s.replaceAll("!!! !!!articleUrl","https://"+backend+"/a/"+pathInfo);

		} else {
			s = s.replaceAll("!!! !!!title","SatGag");
			s = s.replaceAll("!!! !!!imgUrl","https://"+backend+"/android-chrome-512x512.png");
			s = s.replaceAll("!!! !!!articleUrl","https://"+backend);
			s = s.replaceAll("!!! !!!description","Live & Laugh & Sats");
			s = s.replaceAll("!!! !!!width","512");
			s = s.replaceAll("!!! !!!height","512");
		}
		System.out.println("index " + userAgent);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_HTML);
		return new ResponseEntity<>(s.getBytes(StandardCharsets.UTF_8), httpHeaders, HttpStatus.OK);
	}


}
