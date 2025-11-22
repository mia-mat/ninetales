package ws.mia.ninetales.hypixel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.util.HttpUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class RemoteHypixelAPI implements HypixelAPI{
	private static final Logger log = LoggerFactory.getLogger(RemoteHypixelAPI.class);

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final EnvironmentService environmentService;

	public RemoteHypixelAPI(ObjectMapper objectMapper, HttpClient httpClient, EnvironmentService environmentService) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
		this.environmentService = environmentService;
	}

	@Nullable
	@Override
	public String getDiscord(UUID uuid) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.hypixel.net/player?key=%s&uuid=%s".formatted(environmentService.getHypixelAPIKey(), uuid.toString())))
					.GET()
					.header("Content-Type", "application/json")
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (HttpUtil.isSuccess(response.statusCode())) {
				return objectMapper.readTree(response.body()).get("player").get("socialMedia").get("links").get("DISCORD").asText();
			}

			return null;
		} catch (IOException  e) {
			log.warn("Unable to get discord from Hypixel for {}", uuid, e);
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Interrupted while attempting to get discord from Hypixel for {}", uuid, e);
			return null;
		} catch (NullPointerException e) {
			return null; // object mapper can't read, probably means player just doesn't doesn't have hypixel data.
		}
	}

}
