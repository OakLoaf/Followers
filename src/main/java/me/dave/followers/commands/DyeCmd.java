package me.dave.followers.commands;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import me.dave.followers.Followers;

public class DyeCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }
        String prefix = Followers.configManager.getPrefix();
        if (!player.hasPermission("follower.admin.dye")) {
            ChatColorHandler.sendMessage(player, prefix + "§7You have insufficient permissions.");
            return true;
        }
        if (args.length != 1) {
            ChatColorHandler.sendMessage(player, prefix + "§7Incorrect Usage, try §c/dye <hexcolor>");
            return true;
        }
        String color = args[0].replace("#", "");
        if (color.length() != 6 && !(color.length() == 7 && color.startsWith("#"))) {
            ChatColorHandler.sendMessage(player, prefix + "§7Incorrect Usage, try §c/gethexarmor <material> <hexcolor>");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta armorMeta)) {
            ChatColorHandler.sendMessage(player, prefix + "§7Material has to be a form of Leather Armor.");
            return true;
        }
        armorMeta.setColor(getRGBFromHex(color));
        item.setItemMeta(armorMeta);
        return true;
    }

    private Color getRGBFromHex(String hexColour) {
        int red = Integer.valueOf(hexColour.substring(0, 2), 16);
        int green = Integer.valueOf(hexColour.substring(2, 4), 16);
        int blue = Integer.valueOf(hexColour.substring(4, 6), 16);
        return Color.fromRGB(red,green, blue);
    }
}
