package org.enchantedskies.esfollowers.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.commands.Follower;

public class PetUserEvents implements Listener {
    ESFollowers plugin;

    public PetUserEvents(ESFollowers instance) {plugin = instance;}

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if (entity.getType() == EntityType.ARMOR_STAND) return;
        if (entity != Follower.playerPetSet.get(player).getArmorStand()) return;
        player.sendMessage("Interacted with your pet.");
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Follower.playerPetSet.get(player).getArmorStand().setHealth(0);
        Follower.playerPetSet.remove(player);
    }
}
