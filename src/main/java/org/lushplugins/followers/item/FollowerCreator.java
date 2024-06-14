package org.lushplugins.followers.item;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lushplugins.followers.gui.custom.BuilderGui;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.listener.EventListener;

import java.util.Arrays;

public class FollowerCreator implements EventListener {
    private static final ItemStack creatorItem = getOrLoadCreatorItem();

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof ArmorStand armorStand) || !heldItem.isSimilar(creatorItem)) {
            return;
        }

        event.setCancelled(true);
        if (!player.hasPermission("follower.admin.create")) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
            return;
        }

        String armorStandName = armorStand.getCustomName();
        FollowerHandler.Builder followerBuilder = new FollowerHandler.Builder();
        if (armorStandName != null) {
            try {
                followerBuilder.setName(armorStandName);
            } catch (IllegalStateException ignored) {}
        }

        EntityEquipment armorStandEquipment = armorStand.getEquipment();
        if (armorStandEquipment != null) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                followerBuilder.setSlot(equipmentSlot, new ExtendedSimpleItemStack(armorStandEquipment.getItem(equipmentSlot)));
            }
        }

        BuilderGui builderGui = new BuilderGui(player, BuilderGui.Mode.CREATE, followerBuilder);
        builderGui.open();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (!heldItem.isSimilar(creatorItem)) {
                return;
            }

            event.setCancelled(true);

            if (!player.hasPermission("follower.admin.create")) {
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
                return;
            }

            BuilderGui builderGui = new BuilderGui(player, BuilderGui.Mode.CREATE, new FollowerHandler.Builder());
            builderGui.open();
        }
    }

    @EventHandler
    public void onPlayerManipulateArmorStand(PlayerArmorStandManipulateEvent event) {
        ItemStack item = event.getPlayerItem();
        if (!item.isSimilar(creatorItem)) {
            return;
        }

        event.setCancelled(true);
    }

    public static ItemStack getOrLoadCreatorItem() {
        if (creatorItem != null) {
            return creatorItem;
        }

        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta creatorMeta = item.getItemMeta();
        creatorMeta.setDisplayName(ChatColorHandler.translate("<gradient:#FBDA00:#EEFDEA>Follower Creator"));
        creatorMeta.setLore(ChatColorHandler.translate(Arrays.asList("&7Right Click an Armor Stand", "&7to turn it into a new Follower!")));
        creatorMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        creatorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(creatorMeta);
        return item;
    }
}