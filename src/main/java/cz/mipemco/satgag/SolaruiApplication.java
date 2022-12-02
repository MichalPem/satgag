package cz.mipemco.satgag;

import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.net.ssl.SSLException;
import java.io.File;

@SpringBootApplication
@EnableScheduling
public class SolaruiApplication {

	@Value("${lnhost}") public String lnhost;
	@Value("${lnport}") public Integer lnport;
	@Value("${certpath}") public String certpath;
	@Value("${macaroonpath}") public String macaroonpath;

	public static void main(String[] args) {
		SpringApplication.run(SolaruiApplication.class, args);
	}

	@Bean
	public SynchronousLndAPI getApi() throws ClientSideException, SSLException
	{
		return new SynchronousLndAPI(lnhost, lnport, new File(certpath), new File(macaroonpath));

	}

}
