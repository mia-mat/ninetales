package ws.mia.ninetales;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebConfiguration {

	@Bean
	public HttpClient httpClient() {
		return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
