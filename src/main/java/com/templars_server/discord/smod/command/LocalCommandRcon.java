package com.templars_server.discord.smod.command;

import com.templars_server.discord.smod.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class LocalCommandRcon extends LocalCommand {

    public LocalCommandRcon() {
        super("rcon_printall", "Execute the addip rcon command");
        OptionData optionMessage = new OptionData(OptionType.STRING, "message", "The message you want to print", true);
        addOption(optionMessage);
    }

    @Override
    public String execute(Bot bot, SlashCommandInteractionEvent event) {
        // Read options, defaults can be found in Channel
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null) {
            return "Invalid text channel";
        }

        if (event.getMember().getRoles().stream().noneMatch(role -> role.getId().equals(bot.getRconRoleId()))) {
            return "You don't have the required role to command me!";
        }

        String response = bot.getRconClient().printAll(readOption(event, "message"));
        if (response == null || response.isEmpty()) {
            return "Command sent but got no response from the server.";
        }

        return response.replaceAll("\\^[0-9]", "");
    }

}
