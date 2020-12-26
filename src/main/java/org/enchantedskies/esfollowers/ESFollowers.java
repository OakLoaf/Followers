package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.enchantedskies.esfollowers.commands.Follower;
import org.enchantedskies.esfollowers.events.PetUserEvents;

public final class ESFollowers extends JavaPlugin implements Listener {
    public static NamespacedKey petKey;
    Listener[] listeners = new Listener[] {new PetUserEvents(this)};

    @Override
    public void onEnable() {
        registerEvents(listeners);
        getCommand("follower").setExecutor(new Follower());
        petKey = new NamespacedKey(this, "pet");

        // BELOW CODE DOES NOT WORK IDK WHY
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() != EntityType.ARMOR_STAND) continue;
                if (entity.getPersistentDataContainer().has(petKey, PersistentDataType.STRING)) entity.remove();
            }
        }
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
