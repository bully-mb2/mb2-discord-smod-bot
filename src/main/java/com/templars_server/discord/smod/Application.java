package com.templars_server.discord.smod;

import com.templars_server.mb2_log_reader.schema.SmodEvent;
import com.templars_server.util.mqtt.MBMqttClient;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.util.settings.Settings;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.*;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException, LoginException, MqttException {
        LOG.info("======== Starting mb2-discord-smod-bot ========");
        LOG.info("Loading settings");
        Settings settings = new Settings();
        settings.load("application.properties");

        LOG.info("Reading properties");
        String token = settings.get("discord.smod.token");
        String guildId = settings.get("discord.smod.guild.id");
        String rconRoleId = settings.get("discord.smod.rcon.role.id");

        LOG.info("Setting up rcon client");
        RconClient rcon = new RconClient();
        rcon.connect(
                settings.getAddress("rcon.host"),
                settings.get("rcon.password")
        );

        Bot bot = new Bot(guildId, rconRoleId, rcon);
        LOG.info("Registering event callbacks");
        MBMqttClient client = new MBMqttClient();
        client.putEventListener(bot::onSmodEvent, SmodEvent.class);

        LOG.info("Logging in discord");
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.addEventListeners(bot);
        builder.setActivity(Activity.watching("MBII"));
        builder.build();

        LOG.info("Connecting to MQTT broker");
        client.connect(
                "tcp://localhost:" + settings.getInt("mqtt.port"),
                settings.get("mqtt.topic")
        );
    }

}
