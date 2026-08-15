package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.discord.misc.RceService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Component
public class NtEvalCommand extends SlashCommand {

	private static final String COMMAND = "nt-eval";

	private final RceService rceService;
	private final DiscordLogService discordLogService;
	private final EnvironmentService environmentService;

	public NtEvalCommand(RceService rceService, @Lazy DiscordLogService discordLogService, EnvironmentService environmentService) {
		super();

		this.rceService = rceService;
		this.discordLogService = discordLogService;
		this.environmentService = environmentService;
	}


	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Eval")
				.addOption(OptionType.STRING, "line", "line", true)
				.addOption(OptionType.STRING, "imports", "imports", false)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	public List<String> getAllowedUsers() {
		if (environmentService.getRceUsers() == null) return List.of();

		return Stream.of(environmentService.getRceUsers().split(";")).map(String::trim).toList();
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		User discordUser = event.getUser();
		if (!getAllowedUsers().contains(discordUser.getId())) {
			discordLogService.debug(event, "Disallowed");
			event.reply("You're not allowed to do that :(").setEphemeral(true).queue();
			return;
		}

		OptionMapping lineOpt = event.getOption("line");
		OptionMapping importsOpt = event.getOption("imports");

		if (lineOpt == null) {
			event.reply("??").setEphemeral(true).queue();
			return;
		}

		String line = lineOpt.getAsString();
		List<String> imports = importsOpt == null ? List.of() : Arrays.stream(importsOpt.getAsString().split(";")).map(String::trim).toList();

		// be helpful
		if (!line.endsWith("}")) line = line + ";";
		if (!line.contains("return ")) line = "return " + line;

		Object out = rceService.execute(line, imports);

		String reply = "Result:\n```json\n%s\n```".formatted(out.toString());
		event.reply(reply).setEphemeral(true).queue();
		discordLogService.debug(event, reply); // TODO a custom output that actually formats the java properly
	}


}
