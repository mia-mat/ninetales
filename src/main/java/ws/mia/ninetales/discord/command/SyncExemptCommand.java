package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.util.List;
import java.util.function.Consumer;

@Component
public class SyncExemptCommand extends SlashCommand {
	private static final String COMMAND = "nt-say";
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;


	public SyncExemptCommand(MongoUserService mongoUserService, EnvironmentService environmentService, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Toggle Role Sync Exemption for a user")
				.addOption(OptionType.USER, "user", "user", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {

		OptionMapping idOpt = event.getOption("user");
		if(idOpt == null) {
			event.reply("who?").setEphemeral(true).queue();
			return;
		}

		long userId = idOpt.getAsUser().getIdLong();

		if(!mongoUserService.isUserLinked(userId)) {
			event.reply("User not linked!`").setEphemeral(true).queue();
			return;
		}
		NinetalesUser user = mongoUserService.getUser(userId);

		boolean isRoleSyncExemptNew = !user.isRoleSyncExempt();

		mongoUserService.setRoleSyncExempt(userId, !isRoleSyncExemptNew);

		event.reply("<@%s> is now ".formatted(userId) + (isRoleSyncExemptNew ? ""  : "*not*") + " exempt from role sync!").setEphemeral(true).queue();


	}

}