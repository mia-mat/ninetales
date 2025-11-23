package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class MakeVisitorCommand extends SlashCommand {
	private static final String COMMAND = "make-visitor";
	private final EnvironmentService environmentService;
	private final MongoUserService mongoUserService;

	public MakeVisitorCommand(EnvironmentService environmentService, MongoUserService mongoUserService) {
		super();
		this.environmentService = environmentService;
		this.mongoUserService = mongoUserService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Skip the discord application process and give a user the visitor role")
				.addOption(OptionType.USER, "user", "User to amke visitor", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		if(!event.getMember().getUnsortedRoles().contains(event.getGuild().getRoleById(environmentService.getTailRoleId()))) {
			event.reply("you can't do that!").setEphemeral(true).queue();
			return;
		}

		OptionMapping opt = event.getOption("user");
		if(opt == null) {
			event.reply("who?").setEphemeral(true).queue();
			return;
		}

		long userId = opt.getAsUser().getIdLong();

		if(!mongoUserService.isUserLinked(userId)) {
			event.reply("That user isn't linked. If you want to link them manually too, use `/force-link`").setEphemeral(true).queue();
			return;
		}

		NinetalesUser user = mongoUserService.getUser(userId);

		if(user.isDiscordMember()) {
			event.reply("That user is already a visitor (or above)!").setEphemeral(true).queue();
			return;
		}

		if(user.getDiscordApplicationChannelId() != null) {
			event.reply("That user has an open discord application at <#%s>. Accept them there instead.".formatted(user.getDiscordApplicationChannelId())).setEphemeral(true).queue();
			return;
		}

		if(user.getGuildApplicationChannelId() != null) {
			event.reply("That user has an open guild application at <#%s>. Deny them before running this command.".formatted(user.getGuildApplicationChannelId())).setEphemeral(true).queue();
			return;
		}

		mongoUserService.setDiscordMember(user.getDiscordId(), true);
		event.getGuild().retrieveMemberById(user.getDiscordId()).queue(member -> {
			event.getGuild().addRoleToMember(member, event.getGuild().getRoleById(environmentService.getVisitorRoleId())).queue();
		});

		event.reply("Made <@%s> a visitor :3".formatted(user.getDiscordId())).setEphemeral(true).queue();
	}
}
