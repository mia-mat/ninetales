package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.command.SlashCommand;
import ws.mia.ninetales.discord.command.VerifyCommand;

import java.util.List;

@Configuration
public class JDAConfiguration {

	@Bean
	public JDA jda(List<SlashCommand> commands, List<EventListener> listeners, EnvironmentService environmentService) throws InterruptedException {
		JDA jda = JDABuilder
				.createDefault(environmentService.getDiscordBotToken())
				.enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES))
				.addEventListeners(listeners.toArray(new Object[0]))
				.build().awaitReady();

		jda.updateCommands().addCommands(commands.stream().map(SlashCommand::getCommand).toList()).queue();

		return jda;
	}

}
