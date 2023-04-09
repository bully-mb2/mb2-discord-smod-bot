package com.templars_server.discord.smod;

import com.templars_server.discord.smod.command.LocalCommand;
import com.templars_server.discord.smod.command.LocalCommandRcon;
import com.templars_server.discord.smod.command.LocalCommandUnwatch;
import com.templars_server.discord.smod.command.LocalCommandListen;
import com.templars_server.discord.smod.store.ListenerList;
import com.templars_server.mb2_log_reader.schema.SmodEvent;
import com.templars_server.util.rcon.RconClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.*;

public class Bot extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Bot.class);

    private final Map<String, LocalCommand> commandList;
    private final ListenerList listenerList;
    private final String guildId;
    private final String rconRoleId;
    private final RconClient rcon;
    private JDA jda;

    public Bot(String guildId, String rconRoleId, RconClient rcon) {
        this.guildId = guildId;
        this.rconRoleId = rconRoleId;
        this.rcon = rcon;
        commandList = new HashMap<>();
        listenerList = new ListenerList();
        registerCommand(new LocalCommandListen());
        registerCommand(new LocalCommandUnwatch());
        registerCommand(new LocalCommandRcon());
    }

    public void onReady(@Nonnull ReadyEvent event) {
        jda = event.getJDA();
        LOG.info("Loading listener list");
        listenerList.loadFromFile();
    }

    public void onGuildReady(@Nonnull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        if (!guild.getId().equals(guildId)) {
            LOG.info("Not registering commands in guild " + guild.getName() + ", not authorized!");
            return;
        }

        LOG.info("Registering commands for authorized guild " + guild.getName());
        commandList.forEach((name, localCommand) -> guild.upsertCommand(name, localCommand.getDescription())
                .addOptions(localCommand.getOptionData())
                .queue()
        );
        event.getJDA().getPresence().setActivity(Activity.watching(event.getGuild().getName()));
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        LOG.info("Leaving guild " + guild.getName());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        LocalCommand localCommand = commandList.get(event.getName());
        if (localCommand == null) {
            event.reply("Command not found? Somehow? Contact my creator please this shouldn't happen!").queue();
            return;
        }

        if (event.getGuild() == null) {
            event.reply("I'm not authorized to work in direct messages!").queue();
            return;
        }

        if (!event.getGuild().getId().equals(guildId)) {
            event.reply("I'm not authorized to work in this guild!").queue();
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.reply("Only Guild members may command me!").queue();
            return;
        }

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("Only members with the administrator permission may command me!").queue();
            return;
        }


        LOG.info("Executing " + localCommand.getName());
        String reply = localCommand.execute(this, event);
        LOG.info("Result: " + reply);
        event.reply(reply).queue();
    }

    public void onSmodEvent(SmodEvent event) {
        for (String guildId : listenerList.getGuildIds()) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                LOG.error("Can't connect to guild " + guildId);
                return;
            }

            String textChannelId = listenerList.get(guildId);
            TextChannel textChannel = guild.getTextChannelById(textChannelId);
            if (textChannel == null) {
                LOG.error("Can't connect to text channel " + textChannelId);
                return;
            }

            textChannel.sendMessageEmbeds(buildEmbed(event)).queue();
        }
    }

    public RconClient getRconClient() {
        return rcon;
    }

    public String getRconRoleId() {
        return rconRoleId;
    }

    public ListenerList getListenerList() {
        return listenerList;
    }

    private void registerCommand(LocalCommand command) {
        commandList.put(command.getName(), command);
    }

    private MessageEmbed buildEmbed(SmodEvent smodEvent) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("SMOD Command");

        int adminId = smodEvent.getAdminId();
        if (adminId != -1) {
            builder.addField("Admin ID", "" + adminId, false);
        }

        String adminName = smodEvent.getAdminName();
        if (adminName != null) {
            builder.addField("Admin Name", stripAlias(adminName), false);
        }

        int targetSlot = smodEvent.getTargetSlot();
        if (targetSlot != -1) {
            builder.addField("Target Slot", "" + targetSlot, false);
        }

        String targetName = smodEvent.getTargetName();
        if (targetName != null) {
            builder.addField("Target Name", stripAlias(targetName), false);
        }

        String command = smodEvent.getCommand();
        if (command != null) {
            builder.addField("Command", command, false);
        }

        String args = smodEvent.getArgs();
        if (args != null) {
            builder.addField("Arguments", args, false);
        }

        builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
        return builder.build();
    }

    private String stripAlias(String alias) {
        return alias.replaceAll("\\^[0-9]", "");
    }

}
