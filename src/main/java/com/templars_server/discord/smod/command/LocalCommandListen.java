package com.templars_server.discord.smod.command;

import com.templars_server.discord.smod.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LocalCommandListen extends LocalCommand {

    public LocalCommandListen() {
        super("listen", "Listen to smod commands in this channel");
    }

    @Override
    public String execute(Bot bot, SlashCommandInteractionEvent event) {
        // Read options, defaults can be found in Channel
        Guild guild = event.getGuild();
        assert guild != null;
        bot.getListenerList().put(guild.getId(), event.getChannel().getId());
        bot.getListenerList().saveToFile();
        return "Now listening to smod commands in this channel";

    }

}
