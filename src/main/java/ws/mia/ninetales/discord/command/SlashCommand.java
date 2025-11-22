package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class SlashCommand extends ListenerAdapter {

	public abstract CommandData getCommand();

	public abstract void onCommand(SlashCommandInteractionEvent event);

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(event.getName().equals(getCommand().getName())) {
			onCommand(event);
		}
	}

}
