package ws.mia.ninetales.discord.application;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.util.Objects;

@Component
public class ApplicationProcessListener extends ListenerAdapter {

	private final ApplicationService applicationService;
	private final MongoUserService mongoUserService;

	public ApplicationProcessListener(ApplicationService applicationService, MongoUserService mongoUserService) {
		this.applicationService = applicationService;
		this.mongoUserService = mongoUserService;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		super.onMessageReceived(event);
		if(event.getChannelType() != ChannelType.TEXT) return;
		if(event.getAuthor().isBot()) return;

		NinetalesUser ntMsgAuthor = mongoUserService.getUser(event.getAuthor().getIdLong());
		if(ntMsgAuthor == null) return;

		if(Objects.equals(event.getChannel().getIdLong(), ntMsgAuthor.getDiscordApplicationChannelId()) ||
				Objects.equals(event.getChannel().getIdLong(), ntMsgAuthor.getGuildApplicationChannelId())) {
			applicationService.attemptSendNextApplicationProcessMessage(event.getChannel().asTextChannel());
		}

	}


}
