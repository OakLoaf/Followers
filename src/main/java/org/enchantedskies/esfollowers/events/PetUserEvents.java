package org.enchantedskies.esfollowers.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class PetUserEvents implements Listener {
    private final HashMap<UUID, UUID> playerPetMap;

    public PetUserEvents(HashMap<UUID, UUID> hashMap) {
        playerPetMap = hashMap;
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if (entity.getType() != EntityType.ARMOR_STAND) return;
        if (entity != Bukkit.getEntity(playerPetMap.get(player.getUniqueId()))) return;
        player.sendMessage("Interacted with your pet.");
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID petUUID = playerPetMap.get(player.getUniqueId());
        if (petUUID == null) return;
        Entity entity = Bukkit.getEntity(petUUID);
        playerPetMap.remove(player.getUniqueId());
        if (entity == null) return;
        entity.remove();
    }
}
