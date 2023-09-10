package me.dave.followers.commands;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.api.events.FollowersReloadEvent;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.exceptions.ObjectNameLockedException;
import me.dave.followers.export.GeyserSkullExporter;
import me.dave.followers.gui.custom.BuilderGui;
import me.dave.followers.gui.custom.ModerationGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import me.dave.followers.item.FollowerCreator;
import me.dave.followers.Followers;
import me.dave.followers.gui.custom.MenuGui;
import me.dave.followers.data.FollowerHandler;

import java.util.*;

public class FollowerCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (args.length == 1) {
            switch(args[0].toLowerCase()) {
                case "create" -> {
                    if (!sender.hasPermission("follower.admin.create")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
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
                case "disable", "hide" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity != null) {
                        Followers.dataManager.getFollowerUser(player).disableFollowerEntity();
                    }
                    return true;
                }
                case "edit" -> {
                    if (!sender.hasPermission("follower.admin.edit")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower edit <follower_name>"));
                    return true;
                }
                case "enable", "show" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity == null || !followerEntity.isAlive()) {
                        Followers.dataManager.getFollowerUser(player).spawnFollowerEntity();
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
                    }
                    return true;
                }
                case "export" -> {
                    if (!sender.hasPermission("follower.admin.export")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower export <export_type>"));
                    return true;
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
                case "moderate" -> {
                    if (!sender.hasPermission("follower.admin.moderate")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    ModerationGui moderationGui = new ModerationGui(player);
                    moderationGui.openInventory();
                    return true;
                }
                case "randomize" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.random")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                    boolean isRandom = followerUser.isRandomType();
                    followerUser.setRandom(!isRandom);

                    if (!isRandom) {
                        followerUser.randomizeFollowerType();
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
                    }

                    return true;
                }
                case "reload" -> {
                    if (!sender.hasPermission("follower.admin.reload")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    Followers.configManager.reloadConfig(Followers.getInstance());
                    Followers.followerManager.reloadFollowers();
                    Followers.dataManager.reloadFollowerInventories();
                    Followers.getInstance().callEvent(new FollowersReloadEvent());
                    sender.sendMessage(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getLangMessage("reloaded")));
                    return true;
                }
                case "set" -> {
                    ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower set <follower_name>"));
                    return true;
                }
                case "toggle" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity == null || !followerEntity.isAlive()) {
                        Followers.dataManager.getFollowerUser(player).spawnFollowerEntity();
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
                    }
                    else {
                        Followers.dataManager.getFollowerUser(player).disableFollowerEntity();
                    }
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
                    if (follower == null) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerNameFinal));
                    } else {
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
                    if (followerHandler == null) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                    } else {
                        FollowerHandler.Builder followerBuilder = new FollowerHandler.Builder(followerHandler);
                        try {
                            followerBuilder.setName(followerName);
                        } catch (ObjectNameLockedException ignored) {}

                        BuilderGui builderGui = new BuilderGui(player, BuilderGui.Mode.EDIT, followerBuilder.setNameLocked(true));
                        builderGui.openInventory();
                    }
                    return true;
                }
                case "export" -> {
                    if (args[1].equalsIgnoreCase("geysermc")) {
                        try {
                            new GeyserSkullExporter().startExport();
                        } catch (Exception e) {
                            e.printStackTrace();

                            // TODO: Make message configurable
                            ChatColorHandler.sendMessage(sender, "&#ff6969Export failed");
                            return true;
                        }

                        // TODO: Make message configurable
                        ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully exported file to &#66b04f'export/custom-skulls.yml'");
                    } else {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/follower export <export_type>"));
                    }

                    return true;
                }
                case "randomize" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.random")) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("no-permissions"));
                        return true;
                    }

                    FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                    boolean isRandom = Boolean.parseBoolean(args[1]);
                    followerUser.setRandom(isRandom);

                    if (!isRandom) {
                        followerUser.randomizeFollowerType();
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
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
                    if (followerHandler == null) {
                        ChatColorHandler.sendMessage(sender, Followers.configManager.getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                    } else {
                        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                        if (followerUser.isRandomType()) {
                            followerUser.setRandom(false);
                        }

                        FollowerEntity followerEntity = followerUser.getFollowerEntity();
                        if (followerEntity != null && followerEntity.isAlive()) {
                            followerEntity.setType(followerName);
                        } else {
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
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        if (args.length == 1) {
            tabComplete.add("hide");
            tabComplete.add("show");
            tabComplete.add("set");
            tabComplete.add("toggle");
            if (sender.hasPermission("follower.random")) tabComplete.add("randomize");
            if (sender.hasPermission("follower.admin.create")) tabComplete.add("create");
            if (sender.hasPermission("follower.admin.delete")) tabComplete.add("delete");
            if (sender.hasPermission("follower.admin.edit")) tabComplete.add("edit");
            if (sender.hasPermission("follower.admin.export")) tabComplete.add("export");
            if (sender.hasPermission("follower.admin.moderate")) tabComplete.add("moderate");
            if (sender.hasPermission("follower.admin.reload")) tabComplete.add("reload");
        } else if (args.length == 2) {
            switch(args[0].toLowerCase()) {
                case "delete" -> {
                    if (sender.hasPermission("follower.admin.delete")) {
                        tabComplete.addAll(Followers.followerManager.getFollowerNames());
                    }
                }
                case "edit" -> {
                    if (sender.hasPermission("follower.admin.edit")) {
                        tabComplete.addAll(Followers.followerManager.getFollowerNames());
                    }
                }
                case "export" -> {
                    if (sender.hasPermission("follower.admin.export")) {
                        tabComplete.add("GeyserMC");
                    }
                }
                case "randomize" -> {
                    if (sender.hasPermission("follower.random")) {
                        tabComplete.add("true");
                        tabComplete.add("false");
                    }
                }
                case "set" -> {
                    if (sender instanceof Player player) {
                        tabComplete.addAll(Followers.dataManager.getFollowerUser(player).getOwnedFollowerNames());
                    }
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
        if (wordCompletionSuccess) {
            return wordCompletion;
        } else {
            return tabComplete;
        }
    }
}
