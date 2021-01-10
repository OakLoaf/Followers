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
    private final ESFollowers plugin;
    private final FileConfiguration config;
    private final HashSet<UUID> openInvPlayerSet;
    private final HashMap<String, ItemStack> followerSkullMap;

    public FollowerCmd(ESFollowers instance, HashSet<UUID> openInvPlayerSet, HashMap<String, ItemStack> followerSkullMap) {
        plugin = instance;
        config = plugin.getConfig();
        this.openInvPlayerSet = openInvPlayerSet;
        this.followerSkullMap = followerSkullMap;
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
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                plugin.reloadConfig();
                followerSkullMap.clear();
                for (String followerName : config.getKeys(false)) {
                    ConfigurationSection configSection = config.getConfigurationSection(followerName + ".Head");
                    if (configSection == null) continue;
                    String materialStr = configSection.getString("Material", "");
                    Material material = Material.getMaterial(materialStr.toUpperCase());
                    if (material == null) continue;
                    ItemStack item = new ItemStack(material);
                    if (material == Material.PLAYER_HEAD) {
                        String skullType = configSection.getString("SkullType", "");
                        if (skullType.equalsIgnoreCase("custom")) {
                            String skullTexture = configSection.getString("Texture");
                            if (skullTexture != null) item = ESFollowers.skullCreator.getCustomSkull(skullTexture);
                            followerSkullMap.put(followerName, item);
                        } else {
                            String skullUUID = configSection.getString("UUID");
                            if (skullUUID == null || skullUUID.equalsIgnoreCase("error")) {
                                followerSkullMap.put(followerName.toLowerCase(), new ItemStack(Material.PLAYER_HEAD));
                                continue;
                            }
                            ESFollowers.skullCreator.getPlayerSkull(UUID.fromString(skullUUID), plugin).thenAccept(itemStack -> Bukkit.getScheduler().runTask(plugin, runnable -> { followerSkullMap.put(followerName, itemStack); }));
                        }
                    }
                }
                player.sendMessage(ChatColor.GREEN + "ESFollowers has been reloaded.");
                return true;
            } else if (args[0].equalsIgnoreCase("create")) {
                if (!player.hasPermission("followers.admin.create")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                ItemStack creator = new FollowerCreator(plugin, followerSkullMap).getCreatorItem();
                player.getInventory().addItem(creator);
                player.sendMessage("§8§l[§d§lES§8§l] §7You have been given a Follower Creator.");
                return true;
            }
        }
        FollowerGUI followerInv = new FollowerGUI(plugin, player, 1, openInvPlayerSet, followerSkullMap);
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

    public HashMap<String, ItemStack> getFollowerSkullMap() {
        return followerSkullMap;
    }
}
