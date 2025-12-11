package ws.mia.ninetales.discord.command.who;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.command.SlashCommand;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.util.UUID;

@Component
public class DiscordCommand extends SlashCommand {
    private static final String COMMAND = "discord";
    private final MongoUserService mongoUserService;
    private final MojangAPI mojangAPI;
    private final DiscordLogService discordLogService;

    public DiscordCommand(MongoUserService mongoUserService, MojangAPI mojangAPI, @Lazy DiscordLogService discordLogService) {
        super();
        this.mongoUserService = mongoUserService;
        this.mojangAPI = mojangAPI;
        this.discordLogService = discordLogService;
    }


    @Override
    public CommandData getCommand() {
        return Commands.slash(COMMAND, "Find out someone's discord username if you're too shy to ask")
                .addOption(OptionType.STRING, "ign", "minecraft username", true);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping optIgn = event.getOption("ign");
        if (optIgn == null) {
            event.reply("Who?").setEphemeral(true).queue();
            return;
        }

        String ign = optIgn.getAsString();
        UUID uuid = mojangAPI.getUuid(ign);

        if (uuid == null) {
            event.reply("That IGN doesn't seem to exist :(\n-# (Mojang's API may be down)").setEphemeral(true).queue();
            return;
        }

        NinetalesUser ntUser = mongoUserService.getUser(uuid);
        if (ntUser == null) {
            event.reply("Sorry, we couldn't find that user :(\n-# (Have they linked?)").setEphemeral(true).queue();
            return;
        }

        event.getGuild().retrieveMemberById(ntUser.getDiscordId()).queue(discordUser -> {
            if (discordUser == null) {
                event.reply("Discord is having a few issues right now :(\nJust ask them directly!").setEphemeral(true).queue();
                return;
            }

            String rep = "`%s` is <@%s> on Discord!".formatted(ign, discordUser.getId());
            event.reply(rep).setEphemeral(true).queue();
            discordLogService.debug(event, rep);
        });


    }
}
