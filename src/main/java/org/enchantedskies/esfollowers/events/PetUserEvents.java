package org.enchantedskies.esfollowers.events;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
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

    @EventHandler
    public void onPathfind(EntityPathfindEvent e) {
        if (e.getEntity() instanceof Bee) {

            Bee bee = (Bee) e.getEntity();
            Pathfinder pathfinder = bee.getPathfinder();
            pathfinder.setCanFloat(true);
            Player player = Bukkit.getPlayer("Xemor_");
            if (player == null) return;
            if (player.equals(e.getTargetEntity())) return;
            if (player.getLocation().distanceSquared(bee.getLocation()) < 100) {
                pathfinder.moveTo(player);
            }
        }
    }

}
