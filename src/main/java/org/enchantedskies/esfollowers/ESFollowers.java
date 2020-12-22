package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import org.bukkit.plugin.java.JavaPlugin;
import org.enchantedskies.esfollowers.commands.Follower;
import org.enchantedskies.esfollowers.events.PetUserEvents;

public final class ESFollowers extends JavaPlugin implements Listener {
    Listener[] listeners = new Listener[] {new PetUserEvents(this)};

    @Override
    public void onEnable() {
        registerEvents(listeners);
        getCommand("follower").setExecutor(new Follower());
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
