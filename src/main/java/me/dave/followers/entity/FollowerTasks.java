package me.dave.followers.entity;

import me.dave.followers.entity.tasks.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class FollowerTasks {
    private static final HashMap<String, Class<? extends FollowerTask>> tasks = new HashMap<>();

    static {
        register(MovementTask.ID, MovementTask.class);
        register(ParticleTask.ID, ParticleTask.class);
        register(ValidateTask.ID, ValidateTask.class);
        register(VisibilityTask.ID, VisibilityTask.class);
    }

    public static void register(String identifier, Class<? extends FollowerTask> task) {
        tasks.put(identifier, task);
    }

    public static Class<? extends FollowerTask> getClass(String id) {
        return tasks.get(id);
    }

    public static FollowerTask getTask(String id, FollowerEntity followerEntity) {
        try {
            return tasks.get(id).getConstructor(FollowerEntity.class).newInstance(followerEntity);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
