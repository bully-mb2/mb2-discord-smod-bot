package com.templars_server.discord.smod.store;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ListenerList {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerList.class);
    private static final String FILE_NAME = "smodlistenerlist.json";
    private static final Type TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private final Map<String, String> listenerMap;
    private final Gson gson;

    public ListenerList() {
        listenerMap = new HashMap<>();
        gson = new Gson();
    }

    public String put(String guildId, String channelId) {
        return listenerMap.put(guildId, channelId);
    }

    public String get(String guildId) {
        return listenerMap.get(guildId);
    }

    public Set<String> getGuildIds() {
        return listenerMap.keySet();
    }

    public void loadFromFile() {
        LOG.info("Loading from file " + FILE_NAME);
        try (Reader reader = new FileReader(FILE_NAME)) {
            Map<String, String> watchList = gson.fromJson(reader, TYPE);
            if (watchList != null) {
                this.listenerMap.putAll(watchList);
            }
        } catch (IOException | JsonParseException e) {
            LOG.error("Couldn't load listener list", e);
        }
    }

    public void saveToFile() {
        LOG.info("Saving to file " + FILE_NAME);
        try {
            try (Writer writer = new FileWriter(FILE_NAME)) {
                gson.toJson(listenerMap, writer);
            }
        } catch (IOException e) {
            LOG.error("Couldn't save listener list", e);
        }
    }

    public String remove(String guildId) {
        return listenerMap.remove(guildId);
    }

}
