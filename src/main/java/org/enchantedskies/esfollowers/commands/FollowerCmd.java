package org.enchantedskies.esfollowers.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.enchantedskies.esfollowers.FollowerCreator;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerGUI;
import org.enchantedskies.esfollowers.datamanager.FollowerHandler;

import java.util.*;

public class FollowerCmd implements CommandExecutor, TabCompleter {
    private final HashSet<UUID> openInvPlayerSet;

    public FollowerCmd(HashSet<UUID> openInvPlayerSet) {
        this.openInvPlayerSet = openInvPlayerSet;
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
                if (!player.hasPermission("follower.admin.reload")) {
                    sender.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
                    return true;
                }
                ESFollowers.followerManager.reloadFollowers();
                ESFollowers.dataManager.reloadFollowerInventories();
                player.sendMessage(ChatColor.GREEN + "ESFollowers has been reloaded.");
                return true;
            } else if (args[0].equalsIgnoreCase("create")) {
                if (!player.hasPermission("follower.admin.create")) {
                    sender.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
                    return true;
                }
                ItemStack creator = new FollowerCreator().getCreatorItem();
                player.getInventory().addItem(creator);
                player.sendMessage(ESFollowers.prefix + "§7You have been given a Follower Creator.");
                return true;
            }  else if (args[0].equalsIgnoreCase("delete")) {
                if (!player.hasPermission("follower.admin.delete")) {
                    sender.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
                    return true;
                }
                player.sendMessage(ESFollowers.prefix + "§cIncorrect usage: Try /follower delete <follower_name>.");
                return true;
            }
        }
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                if (!player.hasPermission("follower.admin.delete")) {
                    sender.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
                    return true;
                }
                String[] temp = Arrays.copyOfRange(args, 1, args.length);
                StringBuilder followerName = new StringBuilder();
                for (String currString : temp) {
                    followerName.append(currString).append(" ");
                }
                String followerNameFinal = followerName.substring(0, followerName.length() - 1);
                FollowerHandler follower = ESFollowers.followerManager.getFollower(followerNameFinal);
                if (follower == null) player.sendMessage(ESFollowers.prefix + "§cThe Follower " + followerNameFinal + " does not exist.");
                else {
                    ESFollowers.followerManager.removeFollower(followerNameFinal);
                    player.sendMessage(ESFollowers.prefix + "§aThe Follower " + followerNameFinal + " has been deleted.");
                }
                return true;
            }
        }
        FollowerGUI followerInv = new FollowerGUI(player, 1, openInvPlayerSet);
        followerInv.openInventory(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        if (args.length == 1) {
            if (commandSender.hasPermission("follower.admin.reload")) tabComplete.add("reload");
            if (commandSender.hasPermission("follower.admin.create")) tabComplete.add("create");
            if (commandSender.hasPermission("follower.admin.delete")) tabComplete.add("delete");
        } else if (args.length == 2) {
            if (commandSender.hasPermission("follower.admin.delete")) {
                tabComplete.addAll(ESFollowers.followerManager.getFollowers().keySet());
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
