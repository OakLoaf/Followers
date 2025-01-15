package org.lushplugins.followers.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.PlayerInteractAtFollowerEvent;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.utils.menu.AnvilMenu;

public class PacketListener extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case INTERACT_ENTITY -> {
                WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
                if (!packet.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT)) {
                    return;
                }

                int entityId = packet.getEntityId();

                for (Follower follower : Followers.getInstance().getDataManager().getOwnedFollowers()) {
                    WrapperEntity entity = follower.getEntity();
                    if (entity != null && entity.getEntityId() == entityId) {
                        Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                            Followers.getInstance().callEvent(new PlayerInteractAtFollowerEvent(
                                event.getPlayer(),
                                follower,
                                packet.getHand()
                            ));
                        });

                        return;
                    }
                }
            }
            case NAME_ITEM -> {
                AnvilMenu menu = AnvilMenu.getMenu(event.getUser().getUUID());
                if (menu == null) {
                    return;
                }

                WrapperPlayClientNameItem packet = new WrapperPlayClientNameItem(event);
                menu.updateInput(packet.getItemName());
            }
        }
    }
}
