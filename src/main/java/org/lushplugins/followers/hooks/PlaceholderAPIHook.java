package org.lushplugins.followers.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.bukkit.entity.Player;
import org.lushplugins.lushlib.hook.Hook;

public class PlaceholderAPIHook extends Hook {
    private Expansion expansion;

    public PlaceholderAPIHook() {
        super("PlaceholderAPI");
    }

    @Override
    protected void onEnable() {
        expansion = new Expansion();
        expansion.register();
    }

    @Override
    protected void onDisable() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    private static class Expansion extends PlaceholderExpansion {

        public String onPlaceholderRequest(Player player, String params) {
            if (player == null) {
                return null;
            }

            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            switch (params) {
                // Player placeholders
                case "enabled" -> {
                    return String.valueOf(followerUser.isFollowerEnabled());
                }
                case "name" -> {
                    return followerUser.getFollowerType();
                }
                case "pose" -> {
                    Follower follower = Followers.getInstance().getDataManager().getFollowerUser(player).getFollower();

                    if (follower != null) {
                        return follower.getPose().toString().toLowerCase();
                    } else {
                        return "default";
                    }
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
}
