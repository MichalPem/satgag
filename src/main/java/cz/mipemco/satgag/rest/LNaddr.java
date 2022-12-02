package cz.mipemco.satgag.rest;

import cz.mipemco.satgag.dto.Lnurl;
import cz.mipemco.satgag.dto.User;
import cz.mipemco.satgag.jpa.UserRepository;
import cz.mipemco.satgag.ln.LnManager;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.Optional;

/**
 * @author Michal Pemčák
 */
@org.springframework.web.bind.annotation.RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/.well-known/lnurlp")
public class LNaddr
{

	private final LnManager lnManager;
	private final UserRepository userRepository;
	@Value("${backend}")
	public String backend;

	public LNaddr(LnManager lnManager, UserRepository userRepository)
	{
		this.lnManager = lnManager;
		this.userRepository = userRepository;
	}

	@GetMapping("/{user}")
	public ResponseEntity<Object> getLnurl(@PathVariable String user,@RequestParam(name = "amount",required = false) Integer amount)
	{
		try
		{

			Optional<User> u = userRepository.findUserByLnid(user);
			if (u.isPresent())
			{
				String metadata = "[[\"text/identifier\",\"" + user + "@" + backend + "\"],[\"text/plain\",\"Satoshis to " + user + "@" + backend
						+ ".\"]]";
				if (amount != null)
				{
					try
					{
						return ResponseEntity.ok(lnManager.createInvoice(u.get(), amount/1000,metadata));
					} catch (Exception e)
					{
						e.printStackTrace();
						return ResponseEntity.badRequest().build();
					}
				}
				Lnurl lnurl = new Lnurl();
				lnurl.callback = "https://" + backend + "/.well-known/lnurlp/" + user;
				lnurl.maxSendable = 1000000;
				lnurl.minSendable = 1000;
				lnurl.metadata = metadata;
				return ResponseEntity.ok(lnurl);
			}
			return ResponseEntity.notFound().build();
		} catch (Throwable t)
		{
			t.printStackTrace();
			throw t;
		}
	}
}
