package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;

import java.util.HashMap;

public class FollowerTaskRegistry {
    private final HashMap<String, FollowerTask> followerTasks = new HashMap<>();

    public FollowerTaskRegistry() {
        register(new MoveNearTask(TaskId.MOVE_NEAR));
        register(new MoveToTask(TaskId.MOVE_TO));
        register(new ParticleTask(TaskId.PARTICLE_CLOUD, new Particle<>(ParticleTypes.DUST, new ParticleDustData(2, 1, 1, 1))));
        register(new ValidateTask(TaskId.VALIDATE));
        register(new ViewersTask(TaskId.VIEWERS));
        register(new VisibilityTask(TaskId.VISIBILITY));
    }

    public @Nullable FollowerTask get(@NotNull String id) {
        return followerTasks.get(id);
    }

    public void register(@NotNull FollowerTask task) {
        followerTasks.put(task.getId(), task);
        Followers.getInstance().registerListener(task);
    }

    public void unregister(String id) {
        FollowerTask task = followerTasks.get(id);
        unregister(task);
    }

    public void unregister(@NotNull FollowerTask task) {
        followerTasks.remove(task.getId());
        Followers.getInstance().registerListener(task);
    }
}
