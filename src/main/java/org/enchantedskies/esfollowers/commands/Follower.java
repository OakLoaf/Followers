package org.enchantedskies.esfollowers.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.HashMap;
import java.util.UUID;

public class Follower implements CommandExecutor {
    private final ESFollowers plugin;
    private final HashMap<UUID, UUID> playerFollowerMap;

    public Follower(ESFollowers instance, HashMap<UUID, UUID> hashMap) {
        plugin = instance;
        playerFollowerMap = hashMap;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        Player player = (Player) sender;
        if (playerFollowerMap.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have a follower spawned.");
            return true;
        }
        FollowerArmorStand followerArmorStand;
        if (args.length == 1) {
            followerArmorStand = new FollowerArmorStand(plugin, args[0], player);
        } else {
            // open follower gui
            followerArmorStand = new FollowerArmorStand(plugin, "notch", player);
        }

//        followerArmorStand.setHeadSlot(getPlayerSkull(player.getUniqueId()));
//        followerArmorStand.setChestSlot(getColouredArmour(Material.LEATHER_CHESTPLATE, "#7BC28E"));
//        followerArmorStand.setLegsSlot(getColouredArmour(Material.LEATHER_LEGGINGS, "#7BC28E"));
//        followerArmorStand.setFeetSlot(getColouredArmour(Material.LEATHER_BOOTS, "#7BC28E"));
        followerArmorStand.startMovement(0.4);
        playerFollowerMap.put(player.getUniqueId(), followerArmorStand.getArmorStand().getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Follower Spawned.");
        return true;
    }
}
