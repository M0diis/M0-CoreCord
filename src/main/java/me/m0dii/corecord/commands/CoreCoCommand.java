package me.m0dii.corecord.commands;

import me.m0dii.corecord.CoreCord;
import me.m0dii.corecord.utils.Message;
import me.m0dii.corecord.utils.Messenger;
import net.coreprotect.CoreProtect;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoreCoCommand implements CommandExecutor, TabCompleter {
    private static final String MAIN_PERMISSION = "corecord.main";
    private static final String COMMAND_PERMISSION_PREFIX = "corecord.command.";

    private final CoreCord plugin;

    public CoreCoCommand(@NotNull CoreCord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String alias,
                             @NotNull String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    this.plugin.getCfg().reload(this.plugin);

                    Messenger.sendf(sender, plugin.getCfg().getMessage(Message.GAME_CONFIG_RELOAD));
                    return true;
                }

                if (args[0].equalsIgnoreCase("version")) {
                    Messenger.sendf(sender, "&bCoreProtect version: &3" + CoreProtect.getInstance().getPluginMeta().getVersion());
                    Messenger.sendf(sender, "&bCoreCord version: &3" + plugin.getPluginMeta().getVersion());
                    Messenger.sendf(sender, "&bServer version: &3" + plugin.getServer().getVersion());
                    return true;
                }
            }
        }

        if (sender instanceof Player p) {
            if (!p.hasPermission(MAIN_PERMISSION))
                return true;

            if (args.length == 1) {
                if (allowedToUse(args, "reload", p)) {
                    this.plugin.getCfg().reload(this.plugin);

                    Messenger.sendf(p, plugin.getCfg().getMessage(Message.GAME_CONFIG_RELOAD));
                    return true;
                }

                if (allowedToUse(args, "version", p)) {
                    Messenger msg = new Messenger(p);

                    msg.add("&bCoreProtect version: &3" + CoreProtect.getInstance().getPluginMeta().getVersion())
                            .add("&bCoreCord version: &3" + plugin.getPluginMeta().getVersion())
                            .add("&bServer version: &3" + plugin.getServer().getVersion())
                            .add("&bOS: &3" + System.getProperty("os.name"))
                            .add("&bBukkit: &3" + plugin.getServer().getBukkitVersion())
                            .send();
                    return true;
                }
            }
        }

        return true;
    }

    private boolean allowedToUse(String[] args, String cmd, Player pl) {
        return args[0].equalsIgnoreCase(cmd) && pl.hasPermission(COMMAND_PERMISSION_PREFIX + cmd);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completes = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission(COMMAND_PERMISSION_PREFIX + "reload")) {
                completes.add("reload");
            }

            if (sender.hasPermission(COMMAND_PERMISSION_PREFIX + "version")) {
                completes.add("version");
            }
        }

        return completes;
    }
}
