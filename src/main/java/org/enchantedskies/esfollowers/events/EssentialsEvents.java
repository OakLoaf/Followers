package org.enchantedskies.esfollowers.events;

import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.HashMap;
import java.util.UUID;

public class EssentialsEvents implements Listener {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private final HashMap<UUID, UUID> playerFollowerMap = ESFollowers.dataManager.getPlayerFollowerMap();

    @EventHandler
    public void onAFK(AfkStatusChangeEvent event) {
        IUser iUser = event.getAffected();
        Player player = iUser.getBase();
        new BukkitRunnable() {
            public void run() {
//                if (iUser.isAfk()) {
//                    playerFollowerMap.get(player.getUniqueId());
//                    player.sendMessage("noob dance szn");
//                }
            }
        }.runTaskLater(plugin, 1);
    }
}
