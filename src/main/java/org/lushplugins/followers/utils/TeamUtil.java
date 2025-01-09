package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeamUtil {
    private static final String FOLLOWERS_TEAM_NAME = "follower_entities";

    public static void sendCreateFollowerTeamPacket(Player player) {
        WrapperPlayServerTeams packet = createFollowersTeamPacket();

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void sendCreateFollowerTeamPacket(List<Player> players) {
        WrapperPlayServerTeams packet = createFollowersTeamPacket();

        for (Player player : players) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }

    public static void sendAddFollowerTeamPacket(UUID uuid) {
        WrapperPlayServerTeams packet = createTeamsAddEntitiesPacket(
            FOLLOWERS_TEAM_NAME,
            Collections.singletonList(uuid.toString())
        );

        Bukkit.getOnlinePlayers().forEach(player -> {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        });
    }

    public static WrapperPlayServerTeams createTeamsAddEntitiesPacket(String teamName, List<String> entities) {
        return new WrapperPlayServerTeams(
            teamName,
            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
            entities
        );
    }

    private static WrapperPlayServerTeams createFollowersTeamPacket() {
        return new WrapperPlayServerTeams(
            FOLLOWERS_TEAM_NAME,
            WrapperPlayServerTeams.TeamMode.CREATE,
            new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                null,
                null,
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                NamedTextColor.WHITE,
                WrapperPlayServerTeams.OptionData.ALL
            ),
            Followers.getInstance().getDataManager().getOwnedFollowers().stream()
                .map(follower -> {
                    WrapperLivingEntity entity = follower.getEntity();
                    return entity != null ? entity.getUuid().toString() : null;
                })
                .filter(Objects::nonNull)
                .toList()
        );
    }
}
