package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class SignMenuFactory {
    private final Map<Player, SignMenuFactory.Menu> inputs;

    public SignMenuFactory() {
        this.inputs = new HashMap<>();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
    }

    public SignMenuFactory.Menu newMenu(List<String> text) {
        return new SignMenuFactory.Menu(text);
    }

    public final class Menu {

        private final List<String> text;

        private BiPredicate<Player, String[]> response;
        private boolean reopenIfFail;

        private Location location;

        private boolean forceClose;

        public Menu(List<String> text) {
            this.text = text;
        }

        public SignMenuFactory.Menu reopenIfFail(boolean value) {
            this.reopenIfFail = value;
            return this;
        }

        public SignMenuFactory.Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        public void open(Player player) {
            Objects.requireNonNull(player, "player");
            if (!player.isOnline()) {
                return;
            }
            location = player.getLocation();
            location.setY(location.getBlockY() - 4);

            player.sendBlockChange(location, Followers.getInstance().getConfigManager().getSignMaterial().createBlockData());
            player.sendSignChange(
                location,
                text.stream().map(this::color).toList().toArray(new String[4])
            );


            WrapperPlayServerOpenSignEditor packet = new WrapperPlayServerOpenSignEditor(new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()), true);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);

            inputs.put(player, this);
        }

        /**
         * closes the menu. if force is true, the menu will close and will ignore the reopen
         * functionality. false by default.
         *
         * @param player the player
         * @param force  decides whether it will reopen if reopen is enabled
         */
        public void close(Player player, boolean force) {
            this.forceClose = force;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        public void close(Player player) {
            close(player, false);
        }

        private String color(String input) {
            return ChatColor.translateAlternateColorCodes('&', input);
        }
    }

    public class PacketListener extends PacketListenerAbstract {

        public PacketListener() {
            super(PacketListenerPriority.NORMAL);
        }

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.UPDATE_SIGN) {
                WrapperPlayClientUpdateSign packet = new WrapperPlayClientUpdateSign(event);

                Player player = (Player) event.getPlayer();

                SignMenuFactory.Menu menu = inputs.remove(player);
                if (menu == null) {
                    return;
                }

                event.setCancelled(true);

                boolean success = menu.response.test(player, packet.getTextLines());
                if (!success && menu.reopenIfFail && !menu.forceClose) {
                    Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> menu.open(player), 2L);
                }

                Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
                    if (player.isOnline()) {
                        player.sendBlockChange(menu.location, menu.location.getBlock().getBlockData());
                    }
                }, 2L);
            }
        }
    }
}
