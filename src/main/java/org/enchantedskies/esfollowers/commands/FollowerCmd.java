package org.enchantedskies.esfollowers.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.enchantedskies.esfollowers.FollowerCreator;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerGUI;

import java.util.*;

public class FollowerCmd implements CommandExecutor, TabCompleter {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private final FileConfiguration config;
    private final HashSet<UUID> openInvPlayerSet;

    public FollowerCmd(HashSet<UUID> openInvPlayerSet) {
        config = ESFollowers.configManager.getConfig();
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
                if (!player.hasPermission("followers.admin.reload")) {
                    sender.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
                    return true;
                }
                ESFollowers.configManager.reloadConfig();
                ESFollowers.dataManager.reloadFollowerInventories();
                player.sendMessage(ChatColor.GREEN + "ESFollowers has been reloaded.");
                return true;
            } else if (args[0].equalsIgnoreCase("create")) {
                if (!player.hasPermission("followers.admin.create")) {
                    sender.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
                    return true;
                }
                ItemStack creator = new FollowerCreator().getCreatorItem();
                player.getInventory().addItem(creator);
                player.sendMessage(ESFollowers.prefix + "§7You have been given a Follower Creator.");
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
            if (commandSender.hasPermission("followers.admin.reload")) {
                tabComplete.add("reload");
            }
            if (commandSender.hasPermission("follower.admin.create")) {
                tabComplete.add("create");
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
