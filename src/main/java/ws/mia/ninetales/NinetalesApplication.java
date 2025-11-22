package ws.mia.ninetales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NinetalesApplication {

	public static void main(String[] args) {
		SpringApplication.run(NinetalesApplication.class, args);
	}

}
