package me.dave.followers.entity.tasks;

public enum FollowerTaskType {
    ALL("all"),
    MOVEMENT("movement"),
    PARTICLE("particles"),
    VALIDATE("validate"),
    VISIBILITY("visibility");

    public final String identifier;
    FollowerTaskType(String identifier) {
        this.identifier = identifier;
    }
}