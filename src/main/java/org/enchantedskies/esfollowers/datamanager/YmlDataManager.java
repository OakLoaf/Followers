package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerArmorStand;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class YmlDataManager implements Storage {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private final YamlConfiguration config;
    private final File dataFile;
    private final HashMap<UUID, UUID> playerFollowerMap = new HashMap<>();
    private final HashMap<UUID, FollowerUser> uuidToFollowerUser = new HashMap<>();

    public YmlDataManager() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public void loadFollowerUser(UUID uuid) {
        ConfigurationSection configurationSection = config.getConfigurationSection(uuid.toString());
        if (configurationSection == null) {
            configurationSection = config.createSection(uuid.toString());
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            String playerName = player.getName();
            configurationSection.set("name", playerName);
            configurationSection.set("follower", "none");
            configurationSection.set("followerDisplayName", "Unnamed");
            configurationSection.set("followerNameEnabled", Boolean.FALSE);
            configurationSection.set("followerEnabled", Boolean.TRUE);
            plugin.saveConfig();
            uuidToFollowerUser.put(uuid, new FollowerUser(uuid, playerName, "none", "Unnamed", false, false));
            return;
        }
        String name = configurationSection.getString("name");
        String follower = configurationSection.getString("follower");
        String followerDisplayName = configurationSection.getString("followerDisplayName");
        boolean followerNameEnabled = configurationSection.getBoolean("followerNameEnabled");
        boolean followerEnabled = configurationSection.getBoolean("followerEnabled");
        FollowerUser followerUser = new FollowerUser(uuid, name, follower, followerDisplayName, followerNameEnabled, followerEnabled);
        uuidToFollowerUser.put(uuid, followerUser);
    }

    @Override
    public void saveFollowerUser(FollowerUser followerUser) {
        ConfigurationSection configurationSection = config.createSection(followerUser.getUUID().toString());
        configurationSection.set("name", followerUser.getUsername());
        configurationSection.set("follower", followerUser.getFollower());
        configurationSection.set("followerDisplayName", followerUser.getDisplayName());
        configurationSection.set("followerNameEnabled", followerUser.isDisplayNameEnabled());
        configurationSection.set("followerEnabled", followerUser.isFollowerEnabled());
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FollowerUser getFollowerUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return uuidToFollowerUser.getOrDefault(uuid, new FollowerUser(uuid, player.getName(), "none", "Unnamed", false, false));
    }

    public HashMap<UUID, UUID> getPlayerFollowerMap() {
        return playerFollowerMap;
    }

    public void putInPlayerFollowerMap(UUID playerUUID, UUID followerUUID) {
        playerFollowerMap.put(playerUUID, followerUUID);
    }

    public void removeFromPlayerFollowerMap(UUID playerUUID) {
        playerFollowerMap.remove(playerUUID);
    }

    public void reloadFollowerInventories() {
        for (UUID playerUUID : playerFollowerMap.keySet()) {
            UUID followerUUID = playerFollowerMap.get(playerUUID);
            new FollowerArmorStand(getFollowerUser(playerUUID).getFollower(), (ArmorStand) Bukkit.getEntity(followerUUID));
        }
    }
}
