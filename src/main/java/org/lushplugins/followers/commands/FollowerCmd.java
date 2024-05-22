package org.lushplugins.followers.commands;

import org.lushplugins.followers.api.events.FollowersReloadEvent;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.FollowerEntity;
import org.lushplugins.followers.exceptions.ObjectNameLockedException;
import org.lushplugins.followers.export.GeyserSkullExporter;
import org.lushplugins.followers.gui.custom.BuilderGui;
import org.lushplugins.followers.gui.custom.ModerationGui;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerHandler;
import org.lushplugins.followers.gui.custom.MenuGui;
import org.lushplugins.followers.item.FollowerCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.*;

public class FollowerCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            switch(args[0].toLowerCase()) {
                case "create" -> {
                    if (!sender.hasPermission("follower.admin.create")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    ItemStack creator = FollowerCreator.getOrLoadCreatorItem();
                    player.getInventory().addItem(creator);
                    ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("get-follower-creator"));
                    return true;
                }
                case "delete" -> {
                    if (!sender.hasPermission("follower.admin.delete")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers delete <follower_name>"));
                    return true;
                }
                case "disable", "hide" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity != null) {
                        followerUser.disableFollowerEntity();
                    }

                    return true;
                }
                case "display-name" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity != null) {
                        boolean newStatus = !followerUser.isDisplayNameEnabled();
                        followerUser.setDisplayNameEnabled(newStatus);
                        followerEntity.showDisplayName(newStatus);
                    }

                    return true;
                }
                case "edit" -> {
                    if (!sender.hasPermission("follower.admin.edit")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers edit <follower_name>"));
                    return true;
                }
                case "enable", "show" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity == null || !followerEntity.isAlive()) {
                        Followers.getInstance().getDataManager().getFollowerUser(player).spawnFollowerEntity();
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
                    }

                    return true;
                }
                case "export" -> {
                    if (!sender.hasPermission("follower.admin.export")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers export <export_type>"));
                    return true;
                }
                case "messages" -> {
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("reloaded"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-no-name"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-created"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-deleted"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-already-exists"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-default-skull"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("get-follower-creator"));
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("dye-wrong-material"));
                    return true;
                }
                case "moderate" -> {
                    if (!sender.hasPermission("follower.admin.moderate")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
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
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    boolean isRandom = followerUser.isRandomType();
                    followerUser.setRandom(!isRandom);

                    if (!isRandom) {
                        followerUser.randomizeFollowerType();
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
                    }

                    return true;
                }
                case "reload" -> {
                    if (!sender.hasPermission("follower.admin.reload")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    Followers.getInstance().getConfigManager().reloadConfig(Followers.getInstance());
                    Followers.getInstance().getFollowerManager().reloadFollowers();
                    Followers.getInstance().getDataManager().reloadFollowerInventories();
                    Followers.getInstance().callEvent(new FollowersReloadEvent());
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("reloaded"));
                    return true;
                }
                case "rename" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.name")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers rename <name>"));
                    return true;
                }
                case "set" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers set <follower_name>"));
                    return true;
                }
                case "toggle" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity == null || !followerEntity.isAlive()) {
                        Followers.getInstance().getDataManager().getFollowerUser(player).spawnFollowerEntity();
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
                    }
                    else {
                        Followers.getInstance().getDataManager().getFollowerUser(player).disableFollowerEntity();
                    }
                    return true;
                }
            }
        }
        else if (args.length >= 2) {
            switch(args[0].toLowerCase()) {
                case "delete" -> {
                    if (!sender.hasPermission("follower.admin.delete")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    String[] temp = Arrays.copyOfRange(args, 1, args.length);
                    StringBuilder followerName = new StringBuilder();
                    for (String currString : temp) {
                        followerName.append(currString).append(" ");
                    }

                    String followerNameFinal = followerName.substring(0, followerName.length() - 1);
                    FollowerHandler follower = Followers.getInstance().getFollowerManager().getFollower(followerNameFinal);
                    if (follower == null) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerNameFinal));
                    } else {
                        Followers.getInstance().getFollowerManager().removeFollower(followerNameFinal);
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-deleted").replaceAll("%follower%", followerNameFinal));
                        Followers.getInstance().getFollowerManager().refreshAllFollowers();
                    }

                    return true;
                }
                case "display-name" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    boolean newStatus = Boolean.parseBoolean(args[1]);

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    if (followerEntity != null) {
                        followerUser.setDisplayNameEnabled(newStatus);
                        followerEntity.showDisplayName(newStatus);
                    }

                    return true;
                }
                case "edit" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.admin.edit")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    String[] temp = Arrays.copyOfRange(args, 1, args.length);
                    StringBuilder followerNameBuilder = new StringBuilder();
                    for (String currString : temp) {
                        followerNameBuilder.append(currString).append(" ");
                    }
                    String followerName = followerNameBuilder.substring(0, followerNameBuilder.length() - 1);

                    FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerName);
                    if (followerHandler == null) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
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
                            ChatColorHandler.sendMessage(sender, "&#ff6969Export failed");
                            return true;
                        }

                        ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully exported file to &#66b04f'export/custom-skulls.yml'");
                    } else {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers export <export_type>"));
                    }

                    return true;
                }
                case "randomize" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.random")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    boolean isRandom = Boolean.parseBoolean(args[1]);
                    followerUser.setRandom(isRandom);

                    if (!isRandom) {
                        followerUser.randomizeFollowerType();
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
                    }

                    return true;
                }
                case "rename" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("follower.name")) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                        return true;
                    }

                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    FollowerEntity followerEntity = followerUser.getFollowerEntity();
                    String newName = args[1];

                    followerUser.setDisplayName(newName);
                    if (followerEntity != null) {
                        followerEntity.setDisplayName(newName);
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

                    FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerName);
                    if (followerHandler == null) {
                        ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                    } else {
                        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        if (args.length == 1) {
            tabComplete.add("hide");
            tabComplete.add("show");
            tabComplete.add("set");
            tabComplete.add("toggle");
            if (sender.hasPermission("follower.name")) {
                tabComplete.add("display-name");
                tabComplete.add("rename");
            }
            if (sender.hasPermission("follower.random")) {
                tabComplete.add("randomize");
            }
            if (sender.hasPermission("follower.admin.create")) {
                tabComplete.add("create");
            }
            if (sender.hasPermission("follower.admin.delete")) {
                tabComplete.add("delete");
            }
            if (sender.hasPermission("follower.admin.edit")) {
                tabComplete.add("edit");
            }
            if (sender.hasPermission("follower.admin.export")) {
                tabComplete.add("export");
            }
            if (sender.hasPermission("follower.admin.moderate")) {
                tabComplete.add("moderate");
            }
            if (sender.hasPermission("follower.admin.reload")) {
                tabComplete.add("reload");
            }
        } else if (args.length == 2) {
            switch(args[0].toLowerCase()) {
                case "delete" -> {
                    if (sender.hasPermission("follower.admin.delete")) {
                        tabComplete.addAll(Followers.getInstance().getFollowerManager().getFollowerNames());
                    }
                }
                case "display-name" -> {
                    if (sender.hasPermission("follower.name")) {
                        tabComplete.add("true");
                        tabComplete.add("false");
                    }
                }
                case "edit" -> {
                    if (sender.hasPermission("follower.admin.edit")) {
                        tabComplete.addAll(Followers.getInstance().getFollowerManager().getFollowerNames());
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
                case "rename" -> {
                    if (sender.hasPermission("follower.name")) {
                        tabComplete.add("<name>");
                    }
                }
                case "set" -> {
                    if (sender instanceof Player player) {
                        tabComplete.addAll(Followers.getInstance().getDataManager().getFollowerUser(player).getOwnedFollowerNames());
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
