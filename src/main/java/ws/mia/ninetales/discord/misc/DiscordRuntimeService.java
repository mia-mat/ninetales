package ws.mia.ninetales.discord.misc;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ws.mia.poseidon.api.PoseidonClient;
import ws.mia.poseidon.api.PoseidonHttpClient;
import ws.mia.poseidon.api.model.PoseidonContainer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiscordRuntimeService {

	private final DiscordLogService discordLogService;
	private final Environment environment;

	public DiscordRuntimeService(DiscordLogService discordLogService, Environment environment) {
		this.discordLogService = discordLogService;
		this.environment = environment;
	}

	@PostConstruct
	private void init() throws IOException {
		if(List.of(environment.getActiveProfiles()).contains("dev")) {
			File dockerFile = new File(System.getProperty("user.dir"), "Dockerfile");
			String fs = Files.readString(dockerFile.toPath());

			Function<String, String> extractLabel = labelName -> {
				Pattern pattern = Pattern.compile("LABEL\\s+" + Pattern.quote(labelName) + "\\s*=\\s*\"?([^\"\\n]+)\"?");
				Matcher matcher = pattern.matcher(fs);
				return matcher.find() ? matcher.group(1).trim() : null;
			};

			String version = extractLabel.apply("arachne.version");
			String updateNote = extractLabel.apply("ninetales.update-note");

			if(version == null || updateNote == null) {
				discordLogService.warn("Started **(Dev)**", "The bot is now **up**");
				return;
			}

			discordLogService.warn("Started **(Dev)**",
					"The bot is now **up**\n```\nVersion: %s\nUpdate note: %s\n```"
							.formatted(version, updateNote));

			return;
		}

		PoseidonClient poseidonClient = new PoseidonHttpClient("https://poseidon.mia.ws");

		StringBuilder msg = new StringBuilder("The bot is now **up**");

		try {
			String poseidonVersion = poseidonClient.getVersion();
			PoseidonContainer ninetalesContainer = poseidonClient.getContainers().stream().filter(pc -> {
				if (pc.getLabels() == null) return false;
				if (!pc.getLabels().containsKey("github.repository")) return false;
				return pc.getLabels().get("github.repository").endsWith("/ninetales");
			}).findAny().orElseThrow();

			msg.append("\n");
			msg.append("-# Deployed through Poseidon v").append(poseidonVersion);
			msg.append("\n");

			String[] ghImg = ninetalesContainer.getLabels().get("github.image").split("-");
			String commitId = ghImg[ghImg.length - 1];
			msg.append("\nCommit ").append("[").append(commitId).append("](https://gh.mia.ws/ninetales/commit/").append(commitId).append(")");

			@Nullable String updateNote = ninetalesContainer.getLabels().get("ninetales.update-note");
			if (updateNote != null) {
				msg.append("\n\n");
				for (String line : updateNote.split("\\\\n")) {
					msg.append("`").append(line).append("`\n");
				}
			}

			@Nullable String ninetalesVersion = ninetalesContainer.getLabels().get("arachne.version");
			String startMsg = ninetalesVersion != null ? "Started (v**" + ninetalesVersion + "**)" : "Started";
			discordLogService.warn(startMsg, msg.toString());
		} catch (Exception e) {
			discordLogService.warn("Started", "The bot is now **up**");
			throw new RuntimeException("Unable to log Poseidon startup message", e);
		}

	}

}
