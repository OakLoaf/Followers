package org.enchantedskies.esfollowers;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomPet extends EntityBee {
    ESFollowers plugin = ESFollowers.getPlugin(ESFollowers.class);

    public CustomPet(Location location, Player player) {
        super(EntityTypes.BEE, ((CraftWorld) location.getWorld()).getHandle());
        this.setPosition(location.getX(), location.getY(), location.getZ());
        this.setInvulnerable(true);
        this.setSilent(true);
        this.setInvisible(true);
        this.setGoalTarget(((CraftPlayer)player).getHandle(), TargetReason.CUSTOM, true);
        followPlayer(this, player);
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        goalSelector.d().forEach(wrapper -> {
            goalSelector.a(wrapper.j());
        });
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalPet(this, 1, 15));
        goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        goalSelector.a(3, new PathfinderGoalTempt(this, 1.25D, RecipeItemStack.a(TagsItem.FLOWERS), false));
//        for (PathfinderGoalTempt.Type temptType : PathfinderGoalTempt.Type.values()) {
//            goalSelector.b(temptType);
//        }
    }

    public void followPlayer(CustomPet pet, Player player) {
        new BukkitRunnable() {
            public void run() {
                pet.setGoalTarget(((CraftPlayer)player).getHandle(), TargetReason.CUSTOM, true);
                pet.setAggressive(false);
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
