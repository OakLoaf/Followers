package me.dave.followers.item;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.utils.TextInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FollowerCreator implements Listener {
    private final ItemStack creatorItem;

    public FollowerCreator() {
        creatorItem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta creatorMeta = creatorItem.getItemMeta();
        creatorMeta.setDisplayName("Follower Creator");
        creatorMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        creatorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        creatorItem.setItemMeta(creatorMeta);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) return;
        if (!heldItem.isSimilar(creatorItem)) return;
        event.setCancelled(true);
        if (!player.hasPermission("follower.admin.create")) {
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("no-permissions"));
            return;
        }
        if (Followers.dataManager.getActiveArmorStandsSet().contains(armorStand.getUniqueId())) return;
        String armorStandName = armorStand.getCustomName();
        if (armorStandName == null) {
            TextInterface textInterface = new TextInterface();
            textInterface.title("Enter Name:");
            textInterface.placeholder("Enter follower name");
            textInterface.getInput(player, (output) -> {
                if (output.equals("")) {
                    ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-no-name"));
                    return;
                }
                String finalOutput = output.replaceAll("\\.", "-");
                Bukkit.getScheduler().runTask(Followers.getInstance(), () -> Followers.followerManager.createFollower(player, finalOutput, armorStand));
            });
        } else {
            Followers.followerManager.createFollower(player, armorStandName.replaceAll("\\.", "-"), armorStand);
        }
    }

    @EventHandler
    public void onPlayerManipulateArmorStand(PlayerArmorStandManipulateEvent event) {
        ItemStack item = event.getPlayerItem();
        if (!item.isSimilar(creatorItem)) return;
        event.setCancelled(true);
    }

    public ItemStack getCreatorItem() {
        return creatorItem;
    }
}