package org.lushplugins.followers.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.List;

public class GetHexArmorCmd extends Command {

    public GetHexArmorCmd() {
        super("gethexarmor");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }

        if (!player.hasPermission("follower.admin.dye")) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("no-permissions"));
            return true;
        }

        if (args.length != 2) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/gethexarmor <material> <hexcolor>"));
            return true;
        }

        Material material = Material.getMaterial(args[0].toUpperCase());
        String color = args[1].replace("#", "");
        if (material == null || (color.length() != 6 && !(color.length() == 7 && color.startsWith("#")))) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/gethexarmor <material> <hexcolor>"));
            return true;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta armorMeta)) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("dye-wrong-material"));
            return true;
        }

        armorMeta.setColor(getRGBFromHex(color));
        item.setItemMeta(armorMeta);
        player.getInventory().addItem(item);
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        return args.length == 1 && sender.hasPermission("follower.admin.gethexarmor") ? List.of("leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots", "leather_horse_armor") : null;
    }

    private Color getRGBFromHex(String hexColour) {
        int red = Integer.valueOf(hexColour.substring(0, 2), 16);
        int green = Integer.valueOf(hexColour.substring(2, 4), 16);
        int blue = Integer.valueOf(hexColour.substring(4, 6), 16);
        return Color.fromRGB(red,green, blue);
    }
}
