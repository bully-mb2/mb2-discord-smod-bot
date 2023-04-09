package com.templars_server.discord.smod.command;

import com.templars_server.discord.smod.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LocalCommandUnwatch extends LocalCommand {

    public LocalCommandUnwatch() {
        super("stop_listening", "Stop listening to smod commands in this channel");
    }

    @Override
    public String execute(Bot bot, SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return "This command has to be executed in a guild channel";
        }

        String channelId = event.getChannel().getId();
        String removedChannelId = bot.getListenerList().remove(guild.getId());
        if (removedChannelId == null || !removedChannelId.equals(channelId)) {
            return "Not watching anything here!";
        }

        bot.getListenerList().saveToFile();
        return "Not listening to smod commands in this channel anymore!";
    }

}
