package ws.mia.ninetales.discord.application;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class ApplicationArchiveService {

    private final MongoUserService mongoUserService;
    private final EnvironmentService environmentService;
    private final DiscordLogService discordLogService;
    private final MojangAPI mojangAPI;

    public ApplicationArchiveService(MongoUserService mongoUserService, EnvironmentService environmentService, DiscordLogService discordLogService, MojangAPI mojangAPI) {
        this.mongoUserService = mongoUserService;
        this.environmentService = environmentService;
        this.discordLogService = discordLogService;
        this.mojangAPI = mojangAPI;
    }

    /**
     * @param callback guarantee running after archival
     */
    public void archiveApplication(TextChannel applicationChannel, TextChannel tailChannel, Runnable callback) {
        // Note: using complete()'s would make this way more pretty and eliminate the need for a callback. However, thank JDA for making that throw an error inside of
        // calling callbacks because of some edge-cases -.-
        if (applicationChannel == null || tailChannel == null) return;

        NinetalesUser ntUser = mongoUserService.getUserByApplicationChannelId(applicationChannel.getIdLong());
        if (ntUser == null) {
            return;
        }
        ForumChannel forum = null;
        String archContent = null;
        String nPrefix = "";
        if (ntUser.getGuildApplicationChannelId() != null) {
            String arch = environmentService.getGuildApplicationsArchiveForum();
            if (arch == null) return;
            forum = applicationChannel.getGuild().getForumChannelById(arch);
            archContent = "Archive of <@" + ntUser.getDiscordId() + ">'s guild application";
            nPrefix = ntUser.isAwaitingHypixelInvite() ? "Accepted - " : "Denied - ";
        }
        if (ntUser.getDiscordApplicationChannelId() != null) {
            String arch = environmentService.getDiscordApplicationsArchiveForum();
            if (arch == null) return;
            forum = applicationChannel.getGuild().getForumChannelById(arch);
            archContent = "Archive of <@" + ntUser.getDiscordId() + ">'s discord application";
            nPrefix = ntUser.isDiscordMember() ? "Accepted - " : "Denied - ";
        }

        final String finalArchContent = archContent;
        final ForumChannel finalForum = forum;
        final String finalNPrefix = nPrefix;
        applicationChannel.getGuild().retrieveMemberById(ntUser.getDiscordId()).queue(ntMember -> {
            if (finalForum == null) return;

            String mcName = mojangAPI.getUsername(ntUser.getMinecraftUuid());
            if (mcName == null) mcName = applicationChannel.getName();

            final String finalChName = finalNPrefix + mcName;

            finalForum.createForumPost(finalChName, MessageCreateData.fromContent(finalArchContent)).queue(archiveChannel -> {
                applicationChannel.getHistory().retrievePast(100).queue(appChannelRetrievedMsgs -> {
                    List<Message> appChannelMsgs = new ArrayList<>(appChannelRetrievedMsgs);
                    Collections.reverse(appChannelMsgs);

                    // we can send up to 10 embeds at a time, so group.
                    Function<List<MessageEmbed>, List<List<MessageEmbed>>> partitionEmbeds = (l) -> {
                        List<List<MessageEmbed>> partitions = new ArrayList<>();
                        for (int i = 0; i < l.size(); i += 10) {
                            partitions.add(l.subList(i, Math.min(i + 10, l.size())));
                        }
                        return partitions;
                    };

                    Function<List<Message>, List<MessageEmbed>> createMsgEmbeds = (msgs) -> {
                        List<MessageEmbed> embeds = new ArrayList<>();
                        msgs.forEach(message -> {
                            Color c = message.getMember() != null ? message.getMember().getColor() : null;
                            if (message.getMember() != null) {
                                if (ntMember != null && ntMember.getId().equals(message.getMember().getId())) {
                                    c = ntMember.getColor() != null ? ntMember.getColor() : c; // more up-to-date
                                }
                            }

                            EmbedBuilder eb = new EmbedBuilder()
                                    .setAuthor(message.getAuthor().getEffectiveName(), null, message.getAuthor().getAvatarUrl())
                                    .setTimestamp(message.getTimeCreated())
                                    .setDescription(message.getContentDisplay())
                                    .setColor(c);

                            embeds.add(eb.build());
                        });
                        return embeds;
                    };

                    List<List<MessageEmbed>> appChannelPartitions = partitionEmbeds.apply(createMsgEmbeds.apply(appChannelMsgs));

                    if (ntMember != null) {
                        ntMember.getUser().openPrivateChannel().queue(pc -> {
                            String ap = ntUser.getDiscordApplicationChannelId() != null ? "discord" : "guild";
                            pc.sendMessage("Below is a transcript of your recent application to join the Ninetales %s:".formatted(ap)).queue();
                            appChannelPartitions.forEach(m -> {
                                pc.sendMessageEmbeds(m).queue();
                            });
                        });
                    }

                    archiveChannel.getThreadChannel().sendMessage("## Application Channel").queue();
                    appChannelPartitions.forEach(m -> {
                        archiveChannel.getThreadChannel().sendMessageEmbeds(m).queue();
                    });


                    tailChannel.getHistory().retrievePast(100).queue(tailChannelRetrievedMsgs -> {
                        List<Message> tailChannelMsgs = new ArrayList<>(tailChannelRetrievedMsgs);
                        Collections.reverse(tailChannelMsgs);

                        archiveChannel.getThreadChannel().sendMessage("## Tail Channel").queue();
                        List<List<MessageEmbed>> tailChannelPartitions = partitionEmbeds.apply(createMsgEmbeds.apply(tailChannelMsgs));
                        tailChannelPartitions.forEach(m -> {
                            archiveChannel.getThreadChannel().sendMessageEmbeds(m).queue();
                        });

                        discordLogService.debug("Application Archive", "Archived <@" + ntUser.getDiscordId() + ">'s application to " + archiveChannel.getThreadChannel().getJumpUrl());
                        archiveChannel.getThreadChannel().getManager().setLocked(true).queue();

                        callback.run();
                    });

                });
            });

        });

    }

}
