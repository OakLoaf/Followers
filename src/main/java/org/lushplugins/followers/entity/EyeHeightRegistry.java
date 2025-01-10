package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.LocationUtils;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class EyeHeightRegistry {
    public final ConcurrentHashMap<EntityType, Double> eyeHeights = new ConcurrentHashMap<>();

    public void loadEyeHeights(Collection<EntityType> entityTypes) {
        //noinspection DataFlowIssue
        YamlConfiguration eyeHeightData = YamlConfiguration.loadConfiguration(new InputStreamReader(Followers.getInstance().getResource("entity_data/1_21.yml")));
        for (EntityType entityType : entityTypes) {
            if (!this.hasRegisteredEyeHeight(entityType)) {
                double eyeHeight = eyeHeightData.getDouble(entityType.getName().toString() + ".eye-height");
                this.setEyeHeight(entityType, eyeHeight);
            }
        }
    }

    public Location calculateEyeLocation(WrapperLivingEntity entity) {
        double eyeHeight = this.getEyeHeight(entity.getEntityType());
        if (entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
            if (armorStandMeta.isSmall()) {
                eyeHeight = 0.8875;
            }
        }

        double scale = 1;
        for (WrapperPlayServerUpdateAttributes.Property property : entity.getAttributes().getProperties()) {
            if (property.getAttribute() == Attributes.GENERIC_SCALE) {
                scale = property.getValue();
                break;
            }
        }

        return LocationUtils.add(entity.getLocation().clone(), 0, eyeHeight * scale, 0);
    }

    public boolean hasRegisteredEyeHeight(EntityType entityType) {
        return eyeHeights.containsKey(entityType);
    }

    public double getEyeHeight(EntityType entityType) {
        return eyeHeights.getOrDefault(entityType, 2.0);
    }

    public void setEyeHeight(EntityType entityType, double eyeHeight) {
        eyeHeights.put(entityType, eyeHeight);
    }

    public void clear() {
        eyeHeights.clear();
    }
}
