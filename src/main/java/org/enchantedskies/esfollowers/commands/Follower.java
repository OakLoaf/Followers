package org.enchantedskies.esfollowers.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

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
        FollowerArmorStand followerArmorStand;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("gettexture")) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.PLAYER_HEAD) {
                    sender.sendMessage("§cThat is not a player skull.");
                    return true;
                }
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                PlayerProfile skullPlayer = skullMeta.getPlayerProfile();
                if (skullPlayer == null) {
                    sender.sendMessage("§cThis skull does not have an owner.");
                    return true;
                }
                String textureID = skullPlayer
                    .getProperties()
                    .stream()
                    .filter(profileProperty ->profileProperty.getName().equals("textures")).collect(Collectors.toList())
                    .get(0)
                    .getSignature();

                TextComponent message = new TextComponent("§7Skull Texture has been found. §eClick me!");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, textureID));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eClick to save to Clipboard!")));
                player.sendMessage(message);
                return true;
            }
            if (playerFollowerMap.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You already have a follower spawned.");
                return true;
            }
            followerArmorStand = new FollowerArmorStand(plugin, args[0], player);
        } else {
            // open follower gui
            if (playerFollowerMap.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You already have a follower spawned.");
                return true;
            }
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
