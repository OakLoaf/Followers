package me.dave.followers.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return null;
        }

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        switch (params) {
            // Player placeholders
            case "enabled" -> {
                return String.valueOf(followerUser.isFollowerEnabled());
            }
            case "name" -> {
                return followerUser.getFollowerType();
            }
            case "pose" -> {
                return Followers.dataManager.getFollowerUser(player).getFollowerEntity().getPose().toString().toLowerCase();
            }
            case "nickname" -> {
                return followerUser.getDisplayName();
            }
            case "nickname_enabled" -> {
                return String.valueOf(followerUser.isDisplayNameEnabled());
            }
            case "random_enabled" -> {
                return String.valueOf(followerUser.isRandomType());
            }
            case "total" -> {
                return String.valueOf(followerUser.getOwnedFollowerNames().size());
            }
        }

        return "null";
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    public String getIdentifier() {
        return "followers";
    }

    public String getAuthor() {
        return Followers.getInstance().getDescription().getAuthors().toString();
    }

    public String getVersion() {
        return Followers.getInstance().getDescription().getVersion();
    }
}
