package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;
import ws.mia.ninetales.mongo.UserStatus;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Component
public class PasteWelcomeMessageCommand extends SlashCommand{

	private static final String COMMAND = "paste-welcome-message";

	// id constants
	private static final String BUTTON_DISCORD_APPLY_ID = "bNtDiscordApply";
	private static final String BUTTON_GUILD_APPLY_ID = "bNtGuildApply";
	private static final String BUTTON_ASK_QUESTION_ID = "bNtAskQuestion";
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final MojangAPI mojangAPI;

	public PasteWelcomeMessageCommand(MongoUserService mongoUserService, EnvironmentService environmentService, MojangAPI mojangAPI) {
		super();
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.mojangAPI = mojangAPI;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("Ninetales")
				.setDescription(":3")
				.setColor(new Color(215, 193, 248, 239));


		// TODO custom emojis
		net.dv8tion.jda.api.components.buttons.Button bDiscordApply = net.dv8tion.jda.api.components.buttons.Button.of(ButtonStyle.SUCCESS, BUTTON_DISCORD_APPLY_ID, "Apply to join the Ninetales Discord");
		net.dv8tion.jda.api.components.buttons.Button bGuildApply = net.dv8tion.jda.api.components.buttons.Button.of(ButtonStyle.SUCCESS, BUTTON_GUILD_APPLY_ID, "Apply to join the Ninetales Guild");
		net.dv8tion.jda.api.components.buttons.Button bAskQuestion = Button.of(ButtonStyle.SECONDARY, BUTTON_ASK_QUESTION_ID, "Ask a Question");

		event.getChannel().sendMessageEmbeds(embed.build())
				.addComponents(ActionRow.of(bDiscordApply), ActionRow.of(bGuildApply), ActionRow.of(bAskQuestion)).queue();

		event.reply(":3")
				.setEphemeral(true)
				.queue();
	}


	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if(event.getComponentId().equals(BUTTON_DISCORD_APPLY_ID)) {
			if(!mongoUserService.isUserLinked(event.getUser().getIdLong())) {
				event.reply("You need to be linked to apply! Head over to <#%s> first.".formatted(environmentService.getLinkChannelId())).setEphemeral(true).queue();
				return;
			}

			NinetalesUser ntUser = mongoUserService.getUser(event.getUser().getIdLong());
			if(ntUser.getStatus() != UserStatus.OUTSIDER) {
				event.reply("What do you think you're doing here :p").setEphemeral(true).queue();
				return;
			}

			if(ntUser.getDiscordApplicationChannelId() != null || ntUser.getGuildApplicationChannelId() != null) {
				event.reply("You already have an open application, you can't apply again you goober :p").setEphemeral(true).queue();
				return;
			}

			String mcUsername = mojangAPI.getUsername(ntUser.getMinecraftUuid());
			if(mcUsername == null) mcUsername = ntUser.getMinecraftUuid().toString();
			prepareUserStaffChannel(event, mcUsername, environmentService.getDiscordApplicationsCategoryId())
					.setTopic("Ninetales Discord Application for " + mcUsername)
					.queue(tc -> {
				mongoUserService.setDiscordApplicationChannelId(event.getUser().getIdLong(), tc.getIdLong());
				tc.sendMessage("meow").queue();
				tc.sendMessage("uhh, tell us about yourself or something idk").queue();

				event.reply("Head over to <#%s> to fill in your application :3".formatted(tc.getIdLong())).setEphemeral(true).queue();
			});

			return;
		}

		if(event.getComponentId().equals(BUTTON_GUILD_APPLY_ID)) {
			if(!mongoUserService.isUserLinked(event.getUser().getIdLong())) {
				event.reply("You need to be linked to apply! Head over to <#%s> first.".formatted(environmentService.getLinkChannelId())).setEphemeral(true).queue();
				return;
			}

			NinetalesUser ntUser = mongoUserService.getUser(event.getUser().getIdLong());
			if(ntUser.getStatus() == UserStatus.GUILD_MEMBER) {
				event.reply("What do you think you're doing here :p").setEphemeral(true).queue();
				return;
			}

			if(ntUser.getDiscordApplicationChannelId() != null || ntUser.getGuildApplicationChannelId() != null) {
				event.reply("You already have an open application, you can't apply again you goober :p").setEphemeral(true).queue();
				return;
			}

			String mcUsername = mojangAPI.getUsername(ntUser.getMinecraftUuid());
			if(mcUsername == null) mcUsername = ntUser.getMinecraftUuid().toString();
			prepareUserStaffChannel(event, mcUsername, environmentService.getGuildApplicationsCategoryId())
					.setTopic("Ninetales Guild Application for " + mcUsername)
					.queue(tc -> {
						mongoUserService.setGuildApplicationChannelId(event.getUser().getIdLong(), tc.getIdLong());
						tc.sendMessage("meow").queue();
						tc.sendMessage("uhh, tell us about yourself or something idk").queue();

						event.reply("Head over to <#%s> to fill in your application :3".formatted(tc.getIdLong())).setEphemeral(true).queue();
					});

		}

		if(event.getComponentId().equals(BUTTON_ASK_QUESTION_ID)) {

			NinetalesUser ntUser = mongoUserService.getUser(event.getUser().getIdLong());

			if(ntUser.getQuestionChannelId() != null) {
				event.reply("You already have an open questions channel at <#%s>.\nIf you have any additional questions, ask them there :3".formatted(ntUser.getQuestionChannelId())).setEphemeral(true).queue();
				return;
			}

			prepareUserStaffChannel(event, "q-"+event.getUser().getIdLong(), environmentService.getQuestionsCategoryId())
					.queue(tc -> {
						mongoUserService.setQuestionChannelId(ntUser.getDiscordId(), tc.getIdLong());
						tc.sendMessage("meow").queue();
						tc.sendMessage("uhh, ask us a question :3").queue();

						event.reply("Head over to <#%s> to ask your questions :3".formatted(tc.getIdLong())).setEphemeral(true).queue();
					});

		}

	}

	private ChannelAction<TextChannel> prepareUserStaffChannel(ButtonInteractionEvent event, String channelName, String categoryId) {
		return Objects.requireNonNull(event.getGuild()).createTextChannel(channelName, event.getGuild().getCategoryById(categoryId))
				.addRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, List.of(Permission.VIEW_CHANNEL))
				.addRolePermissionOverride(event.getGuild().getRoleById(environmentService.getTailRoleId()).getIdLong(), List.of(Permission.VIEW_CHANNEL), null)
				.addMemberPermissionOverride(event.getMember().getIdLong(), List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
	}

}
