package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.concurrent.CompletableFuture;

public class MoveTask extends AbstractEntityTask {
    private final Player player;
    private final double speed;
    private boolean teleporting = false;

    public MoveTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
        this.speed = Followers.configManager.getSpeed();
    }

    @Override
    public void run() {
        ArmorStand bodyArmorStand = followerEntity.getBodyArmorStand();
        // Cancels the task if the armour stand is dead
        if (bodyArmorStand == null || !bodyArmorStand.isValid()) {
            cancel();
            return;
        }

        // Teleports follower if the player has moved to another world
        // This may be moved to ValidateTask in the future
        if (bodyArmorStand.getWorld() != player.getWorld()) {
            // Previously used teleportToPlayer method
            if (!teleporting) {
                teleporting = true;
                delayedTeleportTo(player, followerEntity, 20).thenAccept(success -> teleporting = false);
            }
            return;
        }

        Location followerLoc = bodyArmorStand.getLocation();
        Vector difference = getDifference(player, bodyArmorStand);

        // Teleports follower to player if the player is too far away
        if (difference.lengthSquared() > 1024) {
            // Previously used teleportToPlayer method
            if (!teleporting) {
                teleporting = true;
                delayedTeleportTo(player, followerEntity, 5).thenAccept(success -> teleporting = false);
            }
            return;
        }

        // Calculates new location and angle of follower based off of the distance to the player
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
        followerLoc.setDirection(difference);

        // Teleports follower
        followerEntity.teleport(player.getLocation().add(1.5, getArmorStandYOffset(bodyArmorStand), 1.5));

        // Limits following code to run once every 2 ticks
        if (Followers.getCurrentTick() % 2 != 0) return;

        // Sets follower head to be looking at the player
        double headPoseX = eulerToDegree(bodyArmorStand.getHeadPose().getX());
        EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, bodyArmorStand), 0, 0);
        if (headPoseX > 60 && headPoseX < 290) {
            if (headPoseX <= 175) newHeadPoseX.setX(60D);
            else newHeadPoseX.setX(290D);
        }
        bodyArmorStand.setHeadPose(newHeadPoseX);
    }

    private static CompletableFuture<Boolean> delayedTeleportTo(Player player, FollowerEntity followerEntity, int delay) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
            if (followerEntity.isAlive()) completableFuture.complete(followerEntity.teleport(player.getLocation()));
            else completableFuture.complete(false);
        }, delay);
        return completableFuture;
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
