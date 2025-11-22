package ws.mia.ninetales;

import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {

	public String getMongoUri() {
		return System.getenv("NINETALES_MONGO_URI");
	}

	public String getMongoUsersCollectionName() {
		String env = System.getenv("NINETALES_USERS_COLLECTION_NAME");
		 return env != null	? env : "users";
	}

	public String getHypixelAPIKey() {
		return System.getenv("HYPIXEL_API_KEY");
	}

	public String getDiscordBotToken() {
		return System.getenv("DISCORD_BOT_TOKEN");
	}

	public String getGuildApplicationsCategoryId() {
		return System.getenv("GUILD_APPLICATIONS_CATEGORY_ID");
	}

	public String getDiscordApplicationsCategoryId() {
		return System.getenv("DISCORD_APPLICATIONS_CATEGORY_ID");
	}

	public String getQuestionsCategoryId() {
		return System.getenv("QUESTIONS_CATEGORY_ID");
	}

	public String getTailRoleId() {
		return System.getenv("TAIL_ROLE_ID");
	}

	public String getLinkChannelId() {
		return System.getenv("LINK_CHANNEL_ID");
	}

}
