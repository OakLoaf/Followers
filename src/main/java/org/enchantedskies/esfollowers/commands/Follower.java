package org.enchantedskies.esfollowers.commands;

import com.destroystokyo.paper.entity.Pathfinder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.enchantedskies.esfollowers.CharacterArmorStand;
import org.enchantedskies.esfollowers.ESFollowers;

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
        Bee bee = world.spawn(player.getLocation(), Bee.class);
        armorStandConnector(player, bee);
        player.sendMessage(ChatColor.GREEN + "Pet Spawned.");
        movementRunnable(player, bee, false);
        return true;
    }

    public void armorStandConnector(Player player, Bee bee) {
        CharacterArmorStand characterArmorStand = new CharacterArmorStand(bee.getLocation(), getPlayerSkull(player), getItemStack(Material.LEATHER_CHESTPLATE), getItemStack(Material.LEATHER_LEGGINGS), getItemStack(Material.LEATHER_BOOTS));
        ArmorStand armorStand = characterArmorStand.getArmorStand();
        new BukkitRunnable() {
            public void run() {
                Location petLoc = bee.getLocation();
                petLoc.setDirection(getDifference(player, armorStand));
                armorStand.teleport(petLoc);

                armorStand.setHeadPose(new EulerAngle(getPitch(player, armorStand), 0, 0));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void movementRunnable(Player player, Bee bee, boolean canFly) {
        Pathfinder pathfinder = bee.getPathfinder();
        pathfinder.setCanFloat(canFly);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location location = bee.getLocation();
                Location eyeLocation = player.getEyeLocation();
                if (location.distanceSquared(eyeLocation) > 225) {
                    bee.teleport(player);
                    pathfinder.stopPathfinding();
                }
                else if (location.distanceSquared(eyeLocation) > 5) {
                    Vector direction = eyeLocation.toVector().subtract(location.toVector()).normalize().multiply(-2);
                    Location newLocation = eyeLocation.add(direction);
                    pathfinder.moveTo(newLocation);
                }
                else {
                    pathfinder.stopPathfinding();
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
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
