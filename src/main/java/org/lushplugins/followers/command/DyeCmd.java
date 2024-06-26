package org.lushplugins.followers.command;

import org.lushplugins.followers.Followers;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

public class DyeCmd extends Command {

    public DyeCmd() {
        super("dye");
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

        if (args.length != 1) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%",  "/dye <hexcolor>"));
            return true;
        }

        String color = args[0].replace("#", "");
        if (color.length() != 6 && !(color.length() == 7 && color.startsWith("#"))) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/dye <hexcolor>"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta armorMeta)) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("dye-wrong-material"));
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
