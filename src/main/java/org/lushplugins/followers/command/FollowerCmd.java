package org.lushplugins.followers.command;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.api.events.FollowersReloadEvent;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.export.GeyserSkullExporter;
import org.lushplugins.followers.gui.BuilderGui;
import org.lushplugins.followers.gui.MenuGui;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.followers.gui.ModerationGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.followers.utils.Converter;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.entity.LivingEntityConfiguration;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.*;

// TODO: Split sub-commands into separate class files
public class FollowerCmd extends Command {

    public FollowerCmd() {
        super("followers");
        addSubCommand(new CreateCmd());
        addSubCommand(new DeleteCmd());
        addSubCommand(new HideCmd("hide"));
        addSubCommand(new HideCmd("disable"));
        addSubCommand(new DisplayNameCmd());
        addSubCommand(new EditCmd());
        addSubCommand(new ShowCmd("show"));
        addSubCommand(new ShowCmd("enable"));
        addSubCommand(new ExportCmd());
        addSubCommand(new MessageCmd());
        addSubCommand(new ModerateCmd());
        addSubCommand(new RandomiseCmd());
        addSubCommand(new ReloadCmd());
        addSubCommand(new RenameCmd());
        addSubCommand(new SetCmd());
        addSubCommand(new ToggleCmd());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }

        MenuGui menuGui = new MenuGui(player);
        menuGui.open();
        return true;
    }

    public static class CreateCmd extends SubCommand {

        public CreateCmd() {
            super("create");
            addRequiredPermission("follower.admin.create");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            FollowerHandler.Builder builder = FollowerHandler.builder();

            Location eyeLocation = player.getEyeLocation();
            RayTraceResult rayTrace = player.getWorld().rayTraceEntities(
                eyeLocation, eyeLocation.getDirection(),
                5,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
            );

            if (rayTrace != null) {
                // Due to our ray trace predicate this is always a LivingEntity
                LivingEntity entity = (LivingEntity) rayTrace.getHitEntity();
                if (entity != null) {
                    builder.entityType(
                        SpigotConversionUtil.fromBukkitEntityType(entity.getType())
                    );

                    String name = entity.getCustomName();
                    if (name != null) {
                        builder.name(name);
                    }

                    // Whilst the bukkit entity is always a LivingEntity it won't always be in EntityLib
                    if (builder.entityConfig() instanceof LivingEntityConfiguration livingEntityConfig) {
                        EntityEquipment entityEquipment = entity.getEquipment();
                        if (entityEquipment != null) {
                            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                                livingEntityConfig.setEquipment(
                                    Converter.convertEquipmentSlot(equipmentSlot),
                                    new ExtendedSimpleItemStack(entityEquipment.getItem(equipmentSlot))
                                );
                            }
                        }
                    }
                }
            }

            BuilderGui builderGui = new BuilderGui(player, BuilderGui.Mode.CREATE, builder);
            builderGui.open();
            return true;
        }
    }

    public static class DeleteCmd extends SubCommand {

        public DeleteCmd() {
            super("delete");
            addRequiredPermission("follower.admin.delete");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (args.length == 0) {
                ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage")
                    .replaceAll("%command-usage%", "/followers delete <follower_name>"));
            } else {
                String followerName = String.join(" ", args);
                FollowerHandler follower = Followers.getInstance().getFollowerManager().getFollower(followerName);
                if (follower == null) {
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist")
                        .replaceAll("%follower%", followerName));
                } else {
                    Followers.getInstance().getFollowerManager().removeFollower(followerName);
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-deleted")
                        .replaceAll("%follower%", followerName));
                    Followers.getInstance().getFollowerManager().refreshAllFollowers();
                }
            }

            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            return args.length == 1 ? new ArrayList<>(Followers.getInstance().getFollowerManager().getFollowerNames()) : null;
        }
    }

    public static class HideCmd extends SubCommand {

        public HideCmd(String name) {
            super(name);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            Follower follower = followerUser.getFollower();
            if (follower != null) {
                followerUser.setFollowerEnabled(false);
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-despawned"));
            }

            return true;
        }
    }

    public static class DisplayNameCmd extends SubCommand {

        public DisplayNameCmd() {
            super("display-name");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.setDisplayNameEnabled(args.length == 0 ? !followerUser.isDisplayNameEnabled() : Boolean.parseBoolean(args[0]));
            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            return args.length == 1 ? List.of("true", "false") : null;
        }
    }

    public static class EditCmd extends SubCommand {

        public EditCmd() {
            super("edit");
            addRequiredPermission("follower.admin.edit");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            if (args.length == 0) {
                ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage")
                    .replaceAll("%command-usage%", "/followers edit <follower_name>"));
            } else {
                String followerName = String.join(" ", args);
                FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerName);
                if (followerHandler == null) {
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                } else {
                    new BuilderGui(
                        player,
                        BuilderGui.Mode.EDIT,
                        FollowerHandler.builder(followerHandler)
                            .name(followerName)
                            .nameLocked(true)
                    ).open();
                }
            }

            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            return args.length == 1 ? new ArrayList<>(Followers.getInstance().getFollowerManager().getFollowerNames()) : null;
        }
    }

    public static class ShowCmd extends SubCommand {

        public ShowCmd(String name) {
            super(name);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            Follower follower = followerUser.getFollower();
            if (follower == null || !follower.isSpawned()) {
                Followers.getInstance().getDataManager().getFollowerUser(player).spawnFollower();
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
            }

            return true;
        }
    }

    public static class ExportCmd extends SubCommand {

        public ExportCmd() {
            super("export");
            addRequiredPermission("follower.admin.export");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (args.length == 0) {
                ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage")
                    .replaceAll("%command-usage%", "/followers export <export_type>"));
            } else {
                if (args[0].equalsIgnoreCase("geysermc")) {
                    try {
                        new GeyserSkullExporter().startExport();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ChatColorHandler.sendMessage(sender, "&#ff6969Export failed");
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully exported file to &#66b04f'export/custom-skulls.yml'");
                } else {
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage").replaceAll("%command-usage%", "/followers export <export_type>"));
                }
            }

            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            return args.length == 1 ? List.of("GeyserMC") : null;
        }
    }

    public static class MessageCmd extends SubCommand {

        public MessageCmd() {
            super("message");
            addRequiredPermission("follower.admin.messages");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            Followers.getInstance().getConfigManager().getLangMessages().forEach((messageName, message) -> ChatColorHandler.sendMessage(sender, message));
            return true;
        }
    }

    public static class ModerateCmd extends SubCommand {

        public ModerateCmd() {
            super("moderate");
            addRequiredPermission("follower.admin.moderate");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            ModerationGui moderationGui = new ModerationGui(player);
            moderationGui.open();
            return true;
        }
    }

    public static class RandomiseCmd extends SubCommand {

        public RandomiseCmd() {
            super("randomise");
            addRequiredPermission("follower.random");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            boolean isRandom = args.length == 0 ? !followerUser.isRandomType() : Boolean.parseBoolean(args[1]);
            followerUser.setRandom(isRandom);
            if (!isRandom) {
                followerUser.randomiseFollowerType();
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
            }

            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            return args.length == 1 ? List.of("true", "false") : null;
        }
    }

    public static class ReloadCmd extends SubCommand {

        public ReloadCmd() {
            super("reload");
            addRequiredPermission("follower.admin.reload");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            Followers.getInstance().getConfigManager().reloadConfig(Followers.getInstance());
            Followers.getInstance().getFollowerManager().reloadFollowers();
            Followers.getInstance().getDataManager().reloadFollowerInventories();
            MenuGui.clearCache();
            Followers.getInstance().callEvent(new FollowersReloadEvent());
            ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("reloaded"));
            return true;
        }
    }

    public static class RenameCmd extends SubCommand {

        public RenameCmd() {
            super("rename");
            addRequiredPermission("follower.name");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            if (args.length == 0) {
                ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage")
                    .replaceAll("%command-usage%", "/followers rename <name>"));
            } else {
                FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                Follower follower = followerUser.getFollower();
                String newName = String.join(" ", args);

                followerUser.setDisplayName(newName);
                if (follower != null) {
                    follower.setDisplayName(newName);
                    ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-name-changed")
                        .replace("%nickname%", newName));
                }
            }

            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            return args.length == 1 ? List.of("<name>") : null;
        }
    }

    public static class SetCmd extends SubCommand {

        public SetCmd() {
            super("set");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            if (args.length == 0) {
                ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("incorrect-usage")
                    .replaceAll("%command-usage%", "/followers set <follower_name>"));
            } else {
                StringBuilder followerNameBuilder = new StringBuilder();
                for (String currString : args) {
                    followerNameBuilder.append(currString).append(" ");
                }
                String followerName = followerNameBuilder.substring(0, followerNameBuilder.length() - 1);

                FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerName);
                if (followerHandler == null) {
                    ChatColorHandler.sendMessage(sender, Followers.getInstance().getConfigManager().getLangMessage("follower-doesnt-exist").replaceAll("%follower%", followerName));
                } else {
                    FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
                    if (followerUser.isRandomType()) {
                        followerUser.setRandom(false);
                    }

                    Follower follower = followerUser.getFollower();
                    if (follower != null && follower.isSpawned()) {
                        follower.setType(followerName);
                    } else {
                        followerUser.setFollowerType(followerName);
                        followerUser.spawnFollower();
                    }
                }
            }

            return true;
        }

        @Override
        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                return null;
            }

            return args.length == 1 ? new ArrayList<>(Followers.getInstance().getDataManager().getFollowerUser(player).getOwnedFollowerNames()) : null;
        }
    }

    public static class ToggleCmd extends SubCommand {

        public ToggleCmd() {
            super("toggle");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);

            String messageKey;
            if (followerUser.isFollowerEnabled()) {
                followerUser.setFollowerEnabled(false);
                messageKey = "follower-despawned";
            } else {
                FollowerHandler followerType = followerUser.getFollowerType();
                if (followerType == null || !player.hasPermission(followerType.getPermission())) {
                    ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-not-selected"));
                    return true;
                }

                followerUser.setFollowerEnabled(true);
                messageKey = "follower-spawned";
            }

            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage(messageKey));
            return true;
        }
    }
}
