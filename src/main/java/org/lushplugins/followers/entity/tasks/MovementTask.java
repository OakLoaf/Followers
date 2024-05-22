package org.lushplugins.followers.entity.tasks;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.concurrent.CompletableFuture;

public class MovementTask extends FollowerTask {
    public static final String ID = "movement";
    private final Player player;
    private final double speed;
    private boolean teleporting = false;

    public MovementTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
        this.speed = Followers.configManager.getSpeed();
    }

    @Override
    public void tick() {
        // Cancels the task if the armour stand is dead
        if (!followerEntity.isBodyEntityValid()) {
            cancel();
            return;
        }

        ArmorStand bodyArmorStand = followerEntity.getBodyEntity();

        // Teleports follower if the player has moved to another world
        // This may be moved to ValidateTask in the future
        if (bodyArmorStand.getWorld() != player.getWorld()) {
            // Previously used teleportToPlayer method
            if (!teleporting) {
                teleporting = true;
                delayedTeleportTo(player, followerEntity, 20).thenAccept(success -> {
                    if (!success) {
                        Followers.dataManager.getFollowerUser(player).respawnFollowerEntity();
                    }

                    teleporting = false;
                });
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
                delayedTeleportTo(player, followerEntity, 5).thenAccept(success -> {
                    if (!success) {
                        Followers.dataManager.getFollowerUser(player).respawnFollowerEntity();
                    }

                    teleporting = false;
                });
            }
            return;
        }

        // Calculates new location and angle of follower based off of the distance to the player
        if (difference.clone().setY(0).lengthSquared() < 6.25) {
            Vector differenceY = difference.clone().setX(0).setZ(0);

            if (Followers.configManager.areHitboxesEnabled()) {
                differenceY.setY(differenceY.getY() - 0.25);
            } else {
                differenceY.setY(differenceY.getY() - 0.7);
            }

            followerLoc.add(differenceY.multiply(speed));
        } else {
            Vector normalizedDifference = difference.clone().normalize();
            double distance = difference.length() - 5;
            if (distance < 1) {
                distance = 1;
            }

            followerLoc.add(normalizedDifference.multiply(speed * distance));
        }
        followerLoc.setDirection(difference);

        // Teleports follower
        boolean tpSuccess = followerEntity.teleport(followerLoc.add(0, getArmorStandYOffset(bodyArmorStand), 0));
        if (!tpSuccess) {
            Followers.dataManager.getFollowerUser(player).respawnFollowerEntity();
        }

        // Limits following code to run once every 2 ticks
        if (Followers.getCurrentTick() % 2 != 0) {
            return;
        }

        // Sets follower head to be looking at the player
        double headPoseX = eulerToDegree(bodyArmorStand.getHeadPose().getX());
        EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, bodyArmorStand), 0, 0);
        if (headPoseX > 60 && headPoseX < 290) {
            if (headPoseX <= 175) {
                newHeadPoseX.setX(60D);
            } else {
                newHeadPoseX.setX(290D);
            }
        }
        bodyArmorStand.setHeadPose(newHeadPoseX);
    }

    @Override
    public String getIdentifier() {
        return "movement";
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public int getPeriod() {
        return 1;
    }

    private static CompletableFuture<Boolean> delayedTeleportTo(Player player, FollowerEntity followerEntity, int delay) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> completableFuture.complete(followerEntity.teleport(player.getLocation())), delay);
        return completableFuture;
    }

    private static double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Followers.getCurrentTick() + armorStand.getEntityId()));
    }

    private static double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.9, 0)).subtract(armorStand.getEyeLocation()).toVector();
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float)(difference.getY() > 0.0D ? -90 : 90);
        } else {
            return Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
        }
    }

    private static Vector getDifference(Player player, ArmorStand armorStand) {
        return player.getEyeLocation().subtract(armorStand.getEyeLocation()).toVector();
    }

    private static double eulerToDegree(double euler) {
        return (euler / (2 * Math.PI)) * 360;
    }
}