package me.dave.followers.entity;

import me.dave.followers.entity.tasks.AbstractTask;

import java.util.HashMap;

public class FollowerTasks {
    private static final HashMap<String, AbstractTask> tasks = new HashMap<>();

    public static void register(AbstractTask task) {
        tasks.put(task.getIdentifier(), task);
    }

    public static AbstractTask getTask(String id) {
        return tasks.get(id);
    }
}
