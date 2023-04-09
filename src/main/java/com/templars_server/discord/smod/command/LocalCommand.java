package com.templars_server.discord.smod.command;

import com.templars_server.discord.smod.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public abstract class LocalCommand {

    private final String name;
    private final String description;
    private final List<OptionData> optionData;

    public LocalCommand(String name, String description) {
        this.name = name;
        this.description = description;
        this.optionData = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<OptionData> getOptionData() {
        return optionData;
    }

    protected void addOption(OptionData option) {
        optionData.add(option);
    }

    protected String readOption(SlashCommandInteractionEvent event, String name) {
        OptionMapping option = event.getOption(name);
        if (option == null) {
            return null;
        }

        return option.getAsString();
    }

    protected int readOptionAsInt(SlashCommandInteractionEvent event, String name) {
        OptionMapping option = event.getOption(name);
        if (option == null) {
            return 0;
        }

        return option.getAsInt();
    }

    public abstract String execute(Bot bot, SlashCommandInteractionEvent event);

}
