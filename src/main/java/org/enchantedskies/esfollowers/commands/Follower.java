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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerGUI;

import java.util.*;
import java.util.stream.Collectors;

public class Follower implements CommandExecutor, TabCompleter {
    private final ESFollowers plugin;
    private final HashMap<UUID, UUID> playerFollowerMap;
    private final HashSet<UUID> playerSet;

    public Follower(ESFollowers instance, HashMap<UUID, UUID> pfMap, HashSet<UUID> guiPlayerSet) {
        plugin = instance;
        playerFollowerMap = pfMap;
        playerSet = guiPlayerSet;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("followers.admin")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "ESFollowers has been reloaded.");
                return true;
            }
        }
        FollowerGUI followerInv = new FollowerGUI(plugin, player, playerSet);
        followerInv.openInventory(player);
        return true;

//            Get Texture Code:
//
//                ItemStack item = player.getInventory().getItemInMainHand();
//                if (item.getType() != Material.PLAYER_HEAD) {
//                    sender.sendMessage("§cThat is not a player skull.");
//                    return true;
//                }
//                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
//                PlayerProfile skullPlayer = skullMeta.getPlayerProfile();
//                if (skullPlayer == null) {
//                    sender.sendMessage("§cThis skull does not have an owner.");
//                    return true;
//                }
//                String textureID = skullPlayer
//                    .getProperties()
//                    .stream()
//                    .filter(profileProperty ->profileProperty.getName().equals("textures")).collect(Collectors.toList())
//                    .get(0)
//                    .getSignature();
//
//                TextComponent message = new TextComponent("§7Skull Texture has been found. §eClick me!");
//                message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, textureID));
//                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eClick to save to Clipboard!")));
//                player.sendMessage(message);
//                return true;
//
//        followerArmorStand.startMovement(0.4);
//        playerFollowerMap.put(player.getUniqueId(), followerArmorStand.getArmorStand().getUniqueId());
//        player.sendMessage(ChatColor.GREEN + "Follower Spawned.");
//        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        if (args.length == 1) {
            tabComplete.add("help");
            if (commandSender.hasPermission("followers.admin") || commandSender.isOp()) {
                tabComplete.add("reload");
            }
        }

        for (String currTab : tabComplete) {
            int currArg = args.length - 1;
            if (currTab.startsWith(args[currArg])) {
                wordCompletion.add(currTab);
                wordCompletionSuccess = true;
            }
        }
        if (wordCompletionSuccess) return wordCompletion;
        return tabComplete;
    }
}
