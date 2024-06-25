package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;

public class Converter {

    public static EquipmentSlot convertEquipmentSlot(org.bukkit.inventory.EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlot.HELMET;
            case CHEST -> EquipmentSlot.CHEST_PLATE;
            case LEGS -> EquipmentSlot.LEGGINGS;
            case FEET -> EquipmentSlot.BOOTS;
            case HAND -> EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
            default -> EquipmentSlot.valueOf(slot.name());
        };
    }

    public static String getEquipmentSlotName(EquipmentSlot slot) {
        return switch (slot) {
            case HELMET -> "head";
            case CHEST_PLATE -> "chest";
            case LEGGINGS -> "legs";
            case BOOTS -> "feet";
            case MAIN_HAND -> "mainHand";
            case OFF_HAND -> "offHand";
            default -> slot.name().toLowerCase();
        };
    }
}
