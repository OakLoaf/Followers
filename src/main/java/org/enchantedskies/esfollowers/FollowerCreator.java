package org.enchantedskies.esfollowers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.enchantedskies.esfollowers.utils.SignMenuFactory;

import java.util.Arrays;

public class FollowerCreator implements Listener {
    private final ItemStack creatorItem;
    private final SignMenuFactory signMenuFactory = new SignMenuFactory();

    public FollowerCreator() {
        creatorItem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta creatorMeta = creatorItem.getItemMeta();
        creatorMeta.setDisplayName("Follower Creator");
        creatorMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        creatorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        creatorItem.setItemMeta(creatorMeta);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        if (!heldItem.isSimilar(creatorItem)) return;
        event.setCancelled(true);
        if (!player.hasPermission("follower.admin.create")) {
            player.sendMessage(ESFollowers.prefix + "§7You have insufficient permissions.");
            return;
        }
        if (ESFollowers.dataManager.getPlayerFollowerMap().containsValue(armorStand.getUniqueId())) return;
        String armorStandName = armorStand.getCustomName();
        if (armorStandName == null) {
            SignMenuFactory.Menu menu = signMenuFactory.newMenu(Arrays.asList("", "^^^^^^^^^^^", "Enter a name", "for the Follower"))
                    .reopenIfFail(true)
                    .response((thisPlayer, strings) -> {
                        if (strings[0].contains(".")) {
                            thisPlayer.sendMessage(ESFollowers.prefix + "§cFollower name cannot contain the character '.'.");
                            return false;
                        }
                        ESFollowers.followerManager.createFollower(player, strings[0], armorStand);
                        return true;
                    });
            menu.open(player);
        } else if (armorStandName.contains(".")) {
            player.sendMessage(ESFollowers.prefix + "§cFollower name cannot contain the character '.'.");
        } else {
            ESFollowers.followerManager.createFollower(player, armorStandName, armorStand);
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