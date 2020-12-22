package org.enchantedskies.esfollowers.events;

import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.enchantedskies.esfollowers.ESFollowers;

public class PetUserEvents implements Listener {
    ESFollowers plugin;

    public PetUserEvents(ESFollowers instance) {plugin = instance;}

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getCustomName() == null)
            return;
        if (event.getRightClicked().getCustomName().contains(event.getPlayer().getName() + "'s Pet")) {
            event.getRightClicked().remove();
            event.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_HUGE,
                event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY(),
                event.getPlayer().getLocation().getZ(), 0);
        }
    }
}
