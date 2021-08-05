package org.enchantedskies.esfollowers;

import me.xemor.userinterface.TextInterface;
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
    private final ESFollowers plugin = ESFollowers.getInstance();
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
        String prefix = ESFollowers.configManager.getPrefix();
        if (!player.hasPermission("follower.admin.create")) {
            player.sendMessage(prefix + "§7You have insufficient permissions.");
            return;
        }
        if (ESFollowers.dataManager.getPlayerFollowerMap().containsKey(armorStand.getUniqueId())) return;
        String armorStandName = armorStand.getCustomName();
        if (armorStandName == null) {

            ///////////////////////////////////////////////////////////////////////////////////////////////
            //                                                                                           //
            //                                          ______                                           //
            //                                         //    \\                                          //
            //                                        //      \\                                         //
            //                                       //   ___  \\                                        //
            //                                      //   |   |  \\                                       //
            //                                     //    |   |   \\                                      //
            //                                    //     |   |    \\                                     //
            //                                   //      |   |     \\                                    //
            //                                  //       |___|      \\                                   //
            //                                 //         ___        \\                                  //
            //                                //         |   |        \\                                 //
            //                               //          |___|         \\                                //
            //                              //                          \\                               //
            //                             ////////////////////////////////                              //
            //                                                                                           //
            //     WARNING DOES NOT CHECK IF '.' IS THERE (CANNOT ALLOW NAMES WITH '.' DUE TO CONFIG     //
            //                                                                                           //
            ///////////////////////////////////////////////////////////////////////////////////////////////

            TextInterface textInterface = new TextInterface();
            textInterface.title("Enter Name:");
            textInterface.placeholder("Enter follower name");
            textInterface.getInput(player, (output) -> {
                if (output.equals("")) output = " ";
                String finalOutput = output;
                Bukkit.getScheduler().runTask(plugin, () -> ESFollowers.followerManager.createFollower(player, finalOutput, armorStand));
            });
        } else if (armorStandName.contains(".")) {
            player.sendMessage(prefix + "§cFollower name cannot contain the character '.'.");
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