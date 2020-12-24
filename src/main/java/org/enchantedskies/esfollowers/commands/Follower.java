package org.enchantedskies.esfollowers.commands;

import com.destroystokyo.paper.entity.Pathfinder;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.enchantedskies.esfollowers.CharacterArmorStand;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Follower implements CommandExecutor {
    ESFollowers plugin = ESFollowers.getPlugin(ESFollowers.class);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        Player player = (Player) sender;
        World world = player.getWorld();
        petMovement(player, 0.1);
//        Bee bee = world.spawn(player.getLocation(), Bee.class);
//        bee.setBaby();
//        bee.setSilent(true);
//        bee.setInvulnerable(true);
//        bee.setInvisible(true);
//        armorStandConnector(player, bee);
        player.sendMessage(ChatColor.GREEN + "Pet Spawned.");
//        movementRunnable(player, bee, false);
        return true;
    }

    public void petMovement(Player player, double speed) {
        CharacterArmorStand characterArmorStand = new CharacterArmorStand(player.getLocation(), getPlayerSkull(player), getItemStack(Material.LEATHER_CHESTPLATE), getItemStack(Material.LEATHER_LEGGINGS), getItemStack(Material.LEATHER_BOOTS));
        ArmorStand armorStand = characterArmorStand.getArmorStand();
        new BukkitRunnable() {
            public void run() {
                Location petLoc = armorStand.getLocation();
                Vector difference = getDifference(player, armorStand);
                if (difference.lengthSquared() >= 2.25) {
                    Vector normalizedDifference = difference.normalize();
                    petLoc.add(normalizedDifference.multiply(speed));
                }
                petLoc.setDirection(difference);
                armorStand.teleport(petLoc.add(0, getArmorStandYOffset(armorStand), 0));
                armorStand.setHeadPose(new EulerAngle(getPitch(player, armorStand), 0, 0));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public double getArmorStandYOffset(ArmorStand armorStand) {
        double y = (Math.PI / 240) * Math.sin(((double) 1/60) * Math.PI * (Bukkit.getCurrentTick() + armorStand.getEntityId()));
        return y;
    }

    public Vector getDifference(Player player, ArmorStand armorStand) {
        return player.getLocation().subtract(armorStand.getLocation()).toVector();
    }

    public double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.4, 0)).subtract(armorStand.getEyeLocation()).toVector();
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float)(difference.getY() > 0.0D ? -90 : 90);
        } else {
            return Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
        }
    }

    public ItemStack getPlayerSkull(Player player) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        if (skullMeta == null) return skullItem;
        skullMeta.setOwningPlayer(player);
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    public ItemStack getItemStack(Material material) {
        return new ItemStack(material);
    }
}
