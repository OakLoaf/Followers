package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import me.tofaa.entitylib.wrapper.WrapperPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;

import java.util.*;
import java.util.stream.Collectors;

public class PacketsHelper {
    public static final String FOLLOWERS_TEAM_NAME = "follower_entities";

    public static void sendPacket(PacketWrapper<?> packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void sendPacket(PacketWrapper<?> packet, Collection<? extends Player> players) {
        for (Player player : players) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }

    public static WrapperPlayServerTeams createFollowersTeamPacket() {
        List<String> entities = Followers.getInstance().getDataManager().getOwnedFollowers().stream()
            .map(follower -> {
                WrapperLivingEntity entity = follower.getEntity();
                return entity != null && !(entity instanceof WrapperPlayer) ? entity.getUuid().toString() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        entities.add("");

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
            entities
        );
    }

    public static WrapperPlayServerTeams createTeamsAddEntitiesPacket(String teamName, List<String> entities) {
        return new WrapperPlayServerTeams(
            teamName,
            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
            (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
            entities
        );
    }
}
