package org.enchantedskies.esfollowers.commands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class GetHexArmor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 2) {
            player.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/gethexarmor <material> <hexcolor>");
            return true;
        }
        Material material = Material.getMaterial(args[0].toUpperCase());
        String color = args[1];
        if (material == null || color.length() != 6) {
            player.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/gethexarmor <material> <hexcolor>");
            return true;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta)) {
            player.sendMessage("§8§l[§d§lES§8§l] §7Material has to be a form of Leather Armor.");
            return true;
        }
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemMeta;
        armorMeta.setColor(getRGBFromHex(color));
        item.setItemMeta(armorMeta);
        player.getInventory().addItem(item);
        return true;
    }

    private Color getRGBFromHex(String hexColour) {
        int red = Integer.valueOf(hexColour.substring(0, 2), 16);
        int green = Integer.valueOf(hexColour.substring(2, 4), 16);
        int blue = Integer.valueOf(hexColour.substring(4, 6), 16);
        return Color.fromRGB(red,green, blue);
    }
}
