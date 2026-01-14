package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

@Component
public class ServerBoostListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ServerBoostListener.class);
    private final EnvironmentService environmentService;

    public ServerBoostListener(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @Override
    public void onGuildMemberRoleAdd(@NonNull GuildMemberRoleAddEvent event) {
        super.onGuildMemberRoleAdd(event);
        if (environmentService.getServerBoostMessageChannelId() != null && event.getRoles().stream().anyMatch(r -> r.getTags().isBoost())) {
            TextChannel announceChannel = event.getGuild().getTextChannelById(environmentService.getServerBoostMessageChannelId());
            if (announceChannel != null) {
                announceChannel.sendMessage("<@%s> boosted the server :3".formatted(event.getUser().getId())).queue();
            }
            log.warn("DEBUG: {}", environmentService.getServerBoostMessageChannelId());
        }
    }
}
