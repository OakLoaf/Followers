package me.dave.followers.entity;

import me.dave.followers.entity.tasks.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class FollowerTasks {
    private static final HashMap<String, Class<? extends AbstractTask>> tasks = new HashMap<>();

    static {
        register("movement", MovementTask.class);
        register("particle", ParticleTask.class);
        register("validate", ValidateTask.class);
        register("visibility", VisibilityTask.class);
    }

    public static void register(String identifier, Class<? extends AbstractTask> task) {
        tasks.put(identifier, task);
    }

    public static Class<? extends AbstractTask> getClass(String id) {
        return tasks.get(id);
    }

    public static AbstractTask getTask(String id, FollowerEntity followerEntity) {
        try {
            return tasks.get(id).getConstructor(FollowerEntity.class).newInstance(followerEntity);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
