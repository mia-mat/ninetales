package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

import java.util.Random;

@Component
public class MemberJoinListener extends ListenerAdapter {

    private static final String[] JOIN_SUFFIXES = {":3", "owo", "uwu", "meow", "^w^", ">w<"};

    private final EnvironmentService environmentService;

    public MemberJoinListener(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @Override
    public void onGuildMemberJoin(@NonNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        if (environmentService.getMemberJoinMessageChannelId() == null) return;
        TextChannel announceChannel = event.getGuild().getTextChannelById(environmentService.getMemberJoinMessageChannelId());
        if(announceChannel != null) {
            announceChannel.sendMessage("<@%s> joined ".formatted(event.getUser().getIdLong()) + JOIN_SUFFIXES[new Random().nextInt(JOIN_SUFFIXES.length-1)]).queue();
        }
    }
}
