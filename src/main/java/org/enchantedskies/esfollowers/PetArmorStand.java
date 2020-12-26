package org.enchantedskies.esfollowers;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;;

public class PetArmorStand {
    private final ArmorStand armorStand;

    public PetArmorStand(Location location, ItemStack headSlot, ItemStack chestSlot, ItemStack legsSlot, ItemStack feetSlot) {
        armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setInvulnerable(true);
        armorStand.setCanPickupItems(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);

        armorStand.getPersistentDataContainer().set(ESFollowers.petKey, PersistentDataType.STRING, "i_am_pet");

        EntityEquipment armorEquipment = armorStand.getEquipment();
        if (armorEquipment != null) {
            if (headSlot != null) armorEquipment.setHelmet(headSlot);
            if (chestSlot != null) armorEquipment.setChestplate(chestSlot);
            if (legsSlot != null) armorEquipment.setLeggings(legsSlot);
            if (feetSlot != null) armorEquipment.setBoots(feetSlot);
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                armorStand.addEquipmentLock(equipmentSlot, lockType);
            }
        }
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }
}
