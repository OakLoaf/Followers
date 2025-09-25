package org.lushplugins.followers.utils.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.types.PlayerMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.gui.button.StringButton;
import org.lushplugins.followers.utils.SkinData;
import org.lushplugins.followers.utils.SkinUtils;
import org.lushplugins.followers.utils.StringUtils;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushlib.utils.SkullCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerConfiguration extends LivingEntityConfiguration {
    private SkinData skin;

    protected PlayerConfiguration(EntityType entityType, ConfigurationSection config) {
        super(entityType, config);

        // TODO: Test with just entering skin value
        String skinValue = config.getString("skin");
        String skinSignature = config.getString("skin-signature");
        if (skinValue != null) {
            if (skinValue.equalsIgnoreCase("mirror")) {
                this.skin = new SkinData("mirror", null);
            } else {
                UUID uuid = StringUtils.asUUID(skinValue);
                if (uuid != null) {
                    this.skin = SkinData.empty();

                    // TODO: Load skin from MineSkin with uuid
                } else {
                    this.skin = new SkinData(skinValue, skinSignature);

                    if (skinSignature == null) {
                        SkinUtils.generateSkin(skinValue).thenAccept(skin -> this.skin.setSignature(skin.texture().data().signature()));
                    }
                }
            }
        } else {
            this.skin = SkinData.empty();
        }
    }

    protected PlayerConfiguration(EntityType entityType) {
        super(entityType);

        this.skin = SkinData.empty();
    }

    public SkinData getSkin() {
        return skin;
    }

    @Override
    public List<ItemButton> getGuiButtons(Gui gui) {
        List<ItemButton> buttons = new ArrayList<>(super.getGuiButtons(gui));

        buttons.add(
            new StringButton(
                "-",
                () -> DisplayItemStack.builder(Material.PLAYER_HEAD)
                    .setDisplayName("&#ffde8aSkin: &f" + (skin.getValue() != null ? skin.getValue() : ""))
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI1ZDVkNmM4YzhhMjk5NzBiNzEyMzUyMTBkNzAyM2JhMWMyZGZlZTRjZDVhNzlhNzliMTY4ZTAyZmQ5YTI4ZSJ9fX0=")
                    .build()
                    .asItemStack(),
                "Enter Player Name:",
                (input) -> true,
                (output, player) -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(output);
                    String skinValue = SkullCreator.getTexture(offlinePlayer);

                    SkinUtils.generateSkin(skinValue).thenAccept(skin -> {
                        this.skin = new SkinData(skinValue, skin.texture().data().signature());
                    });

                    Bukkit.getScheduler().runTask(Followers.getInstance(), gui::open);
                }
            )
        );

        return buttons;
    }

    @Override
    public WrapperEntity createEntity(int entityId, UUID uuid, EntityMeta meta) {
        List<TextureProperty> textureProperties = List.of(
            new TextureProperty(
                "textures",
                skin.getValue(),
                skin.getSignature()
            )
        );

        WrapperPlayer entity = new WrapperPlayer(
            new UserProfile(uuid, "", textureProperties),
            entityId
        );
        entity.setInTablist(false);

        PlayerMeta playerMeta = entity.getEntityMeta(PlayerMeta.class);
        playerMeta.setJacketEnabled(true);
        playerMeta.setLeftSleeveEnabled(true);
        playerMeta.setRightSleeveEnabled(true);
        playerMeta.setLeftLegEnabled(true);
        playerMeta.setRightLegEnabled(true);
        playerMeta.setHatEnabled(true);

        return entity;
    }

    @Override
    public void applyAttributes(WrapperEntity entity) {
        super.applyAttributes(entity);

        if (entity instanceof WrapperPlayer player && !skin.getValue().equalsIgnoreCase("mirror")) {
            player.setTextureProperties(List.of(
                new TextureProperty(
                    "textures",
                    skin.getValue(),
                    skin.getSignature()
                )
            ));
        }
    }

    @Override
    public void save(ConfigurationSection config) {
        super.save(config);

        if (skin != null) {
            config.set("skin", skin.getValue());
            config.set("skin-signature", skin.getSignature());
        }
    }
}
