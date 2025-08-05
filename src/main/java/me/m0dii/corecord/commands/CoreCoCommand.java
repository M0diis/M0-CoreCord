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
                }
            }
        }

        if (sender instanceof Player p) {
            if (!p.hasPermission("corecord.main"))
                return true;

            if (args.length == 1 && p.hasPermission("")) {
                if (allowedToUse(args, "reload", p)) {
                    this.plugin.getCfg().reload(this.plugin);

                    Messenger.sendf(p, plugin.getCfg().getMessage(Message.GAME_CONFIG_RELOAD));
                }

                if (allowedToUse(args, "version", p)) {
                    Messenger msg = new Messenger(p);

                    msg.add("&bCoreProtect version: &3" + CoreProtect.getInstance().getPluginMeta().getVersion())
                            .add("&bCoreCord version: &3" + plugin.getPluginMeta().getVersion())
                            .add("&bServer version: &3" + plugin.getServer().getVersion())
                            .add("&bOS: &3" + System.getProperty("os.name"))
                            .add("&bBukkit: &3" + plugin.getServer().getBukkitVersion())
                            .send();
                }
            }
        }

        return true;
    }

    private boolean allowedToUse(String[] args, String cmd, Player pl) {
        return args[0].equalsIgnoreCase(cmd) && pl.hasPermission("corecord.command." + cmd);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completes = new ArrayList<>();

        if (args.length == 1) {
            completes.add("reload");
            completes.add("version");
        }

        return completes;
    }
}
