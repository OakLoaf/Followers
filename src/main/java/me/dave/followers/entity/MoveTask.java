package me.dave.followers.entity;

import me.dave.followers.Followers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class MoveTask extends BukkitRunnable {
    private final FollowerEntity followerEntity;
    private final Player player;
    private final double speed;

    public MoveTask(FollowerEntity follower) {
        this.followerEntity = follower;
        this.player = follower.owner;
        this.speed = Followers.configManager.getSpeed();
    }

    @Override
    public void run() {
        // TODO: Run general entity validation checkers elsewhere
        if (!followerEntity.bodyArmorStand.isValid() || !player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> followerEntity.kill(), 5);
            cancel();
            return;
        }
        if (followerEntity.bodyArmorStand.getWorld() != player.getWorld()) {
            teleportToPlayer(player, followerEntity.bodyArmorStand, followerEntity.nameArmorStand);
            return;
        }
        if (followerEntity.isPlayerInvisible != player.isInvisible()) {
            followerEntity.setVisible(!player.isInvisible());
            followerEntity.isPlayerInvisible = player.isInvisible();
        }
        Location followerLoc = followerEntity.bodyArmorStand.getLocation();
        Vector difference = getDifference(player, followerEntity.bodyArmorStand);
        if (difference.clone().setY(0).lengthSquared() < 6.25) {
            Vector differenceY = difference.clone().setX(0).setZ(0);
            if (Followers.configManager.areHitboxesEnabled()) differenceY.setY(differenceY.getY() - 0.25);
            else differenceY.setY(differenceY.getY() - 0.7);
            followerLoc.add(differenceY.multiply(speed));
        } else {
            Vector normalizedDifference = difference.clone().normalize();
            double distance = difference.length() - 5;
            if (distance < 1) distance = 1;
            followerLoc.add(normalizedDifference.multiply(speed * distance));
        }
        if (difference.lengthSquared() > 1024) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> teleportToPlayer(player, followerEntity.bodyArmorStand, followerEntity.nameArmorStand), 5);
            return;
        }
        followerLoc.setDirection(difference);
        teleportArmorStands(followerLoc.add(0, getArmorStandYOffset(followerEntity.bodyArmorStand), 0), followerEntity.bodyArmorStand, followerEntity.nameArmorStand);
        if (Followers.getCurrentTick() % 2 != 0) return;
        double headPoseX = eulerToDegree(followerEntity.bodyArmorStand.getHeadPose().getX());
        EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, followerEntity.bodyArmorStand), 0, 0);
        if (headPoseX > 60 && headPoseX < 290) {
            if (headPoseX <= 175) newHeadPoseX.setX(60D);
            else newHeadPoseX.setX(290D);
        }
        followerEntity.bodyArmorStand.setHeadPose(newHeadPoseX);
    }

    private static void teleportToPlayer(Player player, ArmorStand bodyArmorStand, ArmorStand nameArmorStand) {
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
            Location playerLoc = player.getLocation();
            teleportArmorStands(playerLoc.add(1.5, 0, 1.5), bodyArmorStand, nameArmorStand);
        }, 20);
    }

    private static void teleportArmorStands(Location location, ArmorStand bodyArmorStand, ArmorStand nameArmorStand) {
        if (!bodyArmorStand.getLocation().getChunk().isLoaded()) bodyArmorStand.getLocation().getChunk().load();
        bodyArmorStand.teleport(location);
        if (nameArmorStand != null) nameArmorStand.teleport(location.add(0, 1, 0));
    }

    private static double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Followers.getCurrentTick() + armorStand.getEntityId()));
    }

    private static double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.9, 0)).subtract(armorStand.getEyeLocation()).toVector();
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) return (float)(difference.getY() > 0.0D ? -90 : 90);
        else return Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
    }

    private static Vector getDifference(Player player, ArmorStand armorStand) {
        return player.getEyeLocation().subtract(armorStand.getEyeLocation()).toVector();
    }

    private static double eulerToDegree(double euler) {
        return (euler / (2 * Math.PI)) * 360;
    }
}
