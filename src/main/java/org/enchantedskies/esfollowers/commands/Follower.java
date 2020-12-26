package org.enchantedskies.esfollowers.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.enchantedskies.esfollowers.PetArmorStand;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.HashMap;
import java.util.UUID;

public class Follower implements CommandExecutor {
    ESFollowers plugin = ESFollowers.getPlugin(ESFollowers.class);
    public static HashMap<Player, PetArmorStand> playerPetSet = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        Player player = (Player) sender;
        if (playerPetSet.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "You already have a pet spawned.");
            return true;
        }
        PetArmorStand petArmorStand = new PetArmorStand(player.getLocation(), getPlayerSkull(player.getUniqueId()), getColouredArmour(Material.LEATHER_CHESTPLATE, "#7BC28E"), getColouredArmour(Material.LEATHER_LEGGINGS, "#7BC28E"), getColouredArmour(Material.LEATHER_BOOTS, "#7BC28E"));
        petMovement(petArmorStand, player, 0.4);
        playerPetSet.put(player, petArmorStand);
        player.sendMessage(ChatColor.GREEN + "Pet Spawned.");
        return true;
    }

    public void petMovement(PetArmorStand petArmorStand, Player player, double speed) {
        ArmorStand armorStand = petArmorStand.getArmorStand();
        new BukkitRunnable() {
            public void run() {
                Location petLoc = armorStand.getLocation();
                Vector difference = getDifference(player, armorStand);
                if (difference.clone().setY(0).lengthSquared() < 6.25) {
                    Vector differenceY = difference.clone().setX(0).setZ(0);
                    petLoc.add(differenceY.multiply(speed));
                } else {
                    Vector normalizedDifference = difference.clone().normalize();
                    petLoc.add(normalizedDifference.multiply(speed));
                }

                if (difference.lengthSquared() > 1024) {
                    armorStand.teleport(player);
                    return;
                }

                petLoc.setDirection(difference);
                armorStand.teleport(petLoc.add(0, getArmorStandYOffset(armorStand), 0));
                double headPoseX = eulerToDegree(armorStand.getHeadPose().getX());
                EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, armorStand), 0, 0);
                if (headPoseX > 60 && headPoseX < 290) {
                    if (headPoseX <= 175) {
                        newHeadPoseX.setX(60D);
                    } else {
                        newHeadPoseX.setX(290D);
                    }
                }
                armorStand.setHeadPose(newHeadPoseX);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Bukkit.getCurrentTick() + armorStand.getEntityId()));
    }

    public Vector getDifference(Player player, ArmorStand armorStand) {
        return player.getEyeLocation().subtract(armorStand.getEyeLocation()).toVector();
    }

    public double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.4, 0)).subtract(armorStand.getEyeLocation()).toVector();
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float)(difference.getY() > 0.0D ? -90 : 90);
        } else {
            return Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
        }
    }

    public ItemStack getPlayerSkull(UUID uuid) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        if (skullMeta == null) return skullItem;
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    public ItemStack getCustomSkull(String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setPlayerProfile(Bukkit.createProfile(UUID.randomUUID(), null));
        PlayerProfile playerProfile = skullMeta.getPlayerProfile();
        if (playerProfile == null) return skull;
        playerProfile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(playerProfile);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public ItemStack getColouredArmour(Material material, String hexColour) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta)) return item;
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
        int red = Integer.valueOf(hexColour.substring(1, 3), 16 );
        int green = Integer.valueOf(hexColour.substring(3, 5), 16 );
        int blue = Integer.valueOf(hexColour.substring(5, 7), 16 );
        armorMeta.setColor(Color.fromRGB(red, green, blue));
        item.setItemMeta(armorMeta);
        return item;
    }

    public ItemStack getItemStack(Material material) {
        return new ItemStack(material);
    }

    public double eulerToDegree(double euler) {
        return (euler / (2 * Math.PI)) * 360;
    }
}
