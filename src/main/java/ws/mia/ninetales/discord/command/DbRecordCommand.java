package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class DbRecordCommand extends SlashCommand {
	private static final String COMMAND = "db-record";
	private final MongoUserService mongoUserService;

	public DbRecordCommand(MongoUserService mongoUserService) {
		super();
		this.mongoUserService = mongoUserService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "View the database record of a user")
				.addOption(OptionType.USER, "user", "Discord User", true)
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping opt = event.getOption("user");
		if(opt == null) return;
		User user = opt.getAsUser();

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());
		if(ntUser == null) {
			event.reply("Could not find user record for <@%s>.".formatted(user.getId())).setEphemeral(true).queue();
			return;
		}

		event.reply("Mongo record for <@%s>:\n```json\n%s\n```".formatted(user.getId(), ntUser.toJsonString())).setEphemeral(true).queue();
	}
}

