package org.lushplugins.followers.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.PlayerInteractAtFollowerEvent;
import org.lushplugins.followers.entity.Follower;

public class PacketListener extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (event.getPacketType()) {
            case INTERACT_ENTITY -> {
                WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
                if (!packet.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT)) {
                    return;
                }

                int entityId = packet.getEntityId();

                for (Follower follower : Followers.getInstance().getDataManager().getOwnedFollowers()) {
                    WrapperLivingEntity entity = follower.getEntity();
                    if (entity != null && entity.getEntityId() == entityId) {
                        Followers.getInstance().callEvent(new PlayerInteractAtFollowerEvent(
                            (Player) event.getPlayer(),
                            follower,
                            packet.getHand()
                        ));

                        return;
                    }
                }
            }
        }
    }
}
