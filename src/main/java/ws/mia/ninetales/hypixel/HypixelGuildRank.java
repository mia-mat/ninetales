package ws.mia.ninetales.hypixel;

import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

public enum HypixelGuildRank {
	GUILD_MASTER("Guild Master", EnvironmentServiceInjector.environmentService.getTailRoleId()),
	CO_OWNER("Co-Owner", EnvironmentServiceInjector.environmentService.getTailRoleId()),
	TAIL("Tails", EnvironmentServiceInjector.environmentService.getTailRoleId()),
	VULPIX("Vulpix", EnvironmentServiceInjector.environmentService.getVulpixRoleId()),
	EGG("Egg", EnvironmentServiceInjector.environmentService.getEggRoleId()),
	UNKNOWN(null, null);

	private final String hypixelRankName;
	private final String discordRoleId;

	HypixelGuildRank(String hypixelRankName, String discordRoleId) {
		this.hypixelRankName = hypixelRankName;
		this.discordRoleId = discordRoleId;
	}

	public String getHypixelRankName() {
		return hypixelRankName != null ? hypixelRankName : "Unknown";
	}

	@Nullable
	public String getDiscordRoleId() {
		return discordRoleId;
	}

	@Nullable
	public Role getRole(Guild guild){
		if(getDiscordRoleId() == null) return null;
		return guild.getRoleById(getDiscordRoleId());
	}

	public static HypixelGuildRank fromHypixel(String hypixelRankName) {
		for (HypixelGuildRank rank : values()) {
			if (rank.getHypixelRankName().equals(hypixelRankName)) {
				return rank;
			}
		}
		return UNKNOWN;
	}

	@Component
	public static class EnvironmentServiceInjector {
		private static EnvironmentService environmentService;

		public EnvironmentServiceInjector(EnvironmentService environmentService) {
			if(EnvironmentServiceInjector.environmentService == null) {
				EnvironmentServiceInjector.environmentService = environmentService;
			}
		}

	}

}
