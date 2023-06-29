package me.dave.followers.commands;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.exceptions.ObjectNameLockedException;
import me.dave.followers.gui.BuilderGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import me.dave.followers.item.FollowerCreator;
import me.dave.followers.Followers;
import me.dave.followers.gui.MenuGui;
import me.dave.followers.data.FollowerHandler;

import java.util.*;

public class FollowerCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (args.length == 1) {
            switch(args[0].toLowerCase()) {
                case "reload" -> {
                    if (!sender.hasPermission("follower.admin.reload")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }
                    Followers.configManager.reloadConfig(Followers.getInstance());
                    Followers.followerManager.reloadFollowers();
                    Followers.dataManager.reloadFollowerInventories();
                    sender.sendMessage(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getLangMessage("reloaded")));
                    return true;
                }
                case "create" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    if (!player.hasPermission("follower.admin.create")) {
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }
                    ItemStack creator = FollowerCreator.getOrLoadCreatorItem();
                    player.getInventory().addItem(creator);
                    ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("get-follower-creator"));
                    return true;
                }
                case "delete" -> {
                    if (!sender.hasPermission("follower.admin.delete")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower delete <follower_name>"));
                    return true;
                }
                case "disable" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
                    if (followerUser.getFollowerEntity() != null) {
                        Followers.dataManager.getFollowerUser(player.getUniqueId()).disableFollowerEntity();
                    }
                }
                case "edit" -> {
                    if (!sender.hasPermission("follower.admin.edit")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower edit <follower_name>"));
                    return true;
                }
                case "enable" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
                    if (followerUser.getFollowerEntity() == null) {
                        Followers.dataManager.getFollowerUser(player.getUniqueId()).spawnFollowerEntity();
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
                    }
                }
                case "messages" -> {
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("reloaded"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-spawned"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-no-name"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-created"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-deleted"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-already-exists"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-default-skull"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("get-follower-creator"));
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("dye-wrong-material"));
                    return true;
                }
                case "set" -> {
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower set <follower_name>"));
                    return true;
                }
            }
        }
        if (args.length >= 2) {
            switch(args[0].toLowerCase()) {
                case "delete" -> {
                    if (!sender.hasPermission("follower.admin.delete")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }
                    String[] temp = Arrays.copyOfRange(args, 1, args.length);
                    StringBuilder followerName = new StringBuilder();
                    for (String currString : temp) {
                        followerName.append(currString).append(" ");
                    }
                    String followerNameFinal = followerName.substring(0, followerName.length() - 1);
                    FollowerHandler follower = Followers.followerManager.getFollower(followerNameFinal);
                    if (follower == null) ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerNameFinal));
                    else {
                        Followers.followerManager.removeFollower(followerNameFinal);
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-deleted").replaceAll("%follower%", followerNameFinal));
                        Followers.followerManager.refreshAllFollowers();
                    }
                    return true;
                }
                case "edit" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.admin.edit")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    String[] temp = Arrays.copyOfRange(args, 1, args.length);
                    StringBuilder followerNameBuilder = new StringBuilder();
                    for (String currString : temp) {
                        followerNameBuilder.append(currString).append(" ");
                    }
                    String followerName = followerNameBuilder.substring(0, followerNameBuilder.length() - 1);

                    FollowerHandler followerHandler = Followers.followerManager.getFollower(followerName);
                    if (followerHandler == null) ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                    else {
                        FollowerHandler.Builder followerBuilder = new FollowerHandler.Builder(followerHandler);
                        try {
                            followerBuilder.setName(followerName);
                        } catch (ObjectNameLockedException ignored) {}

                        BuilderGui builderGui = new BuilderGui(player, BuilderGui.Mode.EDIT, followerBuilder.setNameLocked(true));
                        builderGui.openInventory();
                    }
                    return true;
                }
                case "set" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    String[] temp = Arrays.copyOfRange(args, 1, args.length);
                    StringBuilder followerNameBuilder = new StringBuilder();
                    for (String currString : temp) {
                        followerNameBuilder.append(currString).append(" ");
                    }
                    String followerName = followerNameBuilder.substring(0, followerNameBuilder.length() - 1);

                    FollowerHandler followerHandler = Followers.followerManager.getFollower(followerName);
                    if (followerHandler == null) ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                    else {
                        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
                        if (followerUser != null) {
                            followerUser.setFollowerType(followerName);
                            followerUser.spawnFollowerEntity();
                        }
                    }
                    return true;
                }
            }
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        MenuGui menuGui = new MenuGui(player);
        menuGui.openInventory();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        if (args.length == 1) {
            if (commandSender.hasPermission("follower.admin.create")) tabComplete.add("create");
            if (commandSender.hasPermission("follower.admin.delete")) tabComplete.add("delete");
            if (commandSender.hasPermission("follower.admin.edit")) tabComplete.add("edit");
            if (commandSender.hasPermission("follower.admin.reload")) tabComplete.add("reload");
        } else if (args.length == 2) {
            switch(args[0].toLowerCase()) {
                case "delete" -> {
                    if (commandSender.hasPermission("follower.admin.delete")) tabComplete.addAll(Followers.followerManager.getFollowerNames());
                }
                case "edit" -> {
                    if (commandSender.hasPermission("follower.admin.edit")) tabComplete.addAll(Followers.followerManager.getFollowerNames());
                }
                case "set" -> {
                    if (commandSender instanceof Player player) tabComplete.addAll(Followers.dataManager.getFollowerUser(player.getUniqueId()).getOwnedFollowerNames());
                }
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
