package me.m0dii.corecord.listeners;

import me.m0dii.corecord.CoreCord;
import me.m0dii.corecord.utils.Config;
import me.m0dii.corecord.utils.Messenger;
import me.m0dii.corecord.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerJoin implements Listener {
    private final CoreCord plugin;
    private final Config cfg;

    public PlayerJoin(@NotNull CoreCord plugin) {
        this.cfg = plugin.getCfg();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () ->
        {
            Player p = e.getPlayer();

            if (!cfg.isNotifyUpdate()) {
                return;
            }

            if (p.hasPermission("corecord.update.notify")) {
                new UpdateChecker(this.plugin, 91863).getVersion(ver ->
                {
                    String curr = this.plugin.getPluginMeta().getVersion();

                    if (!curr.equals(ver.replace("v", ""))
                            && p.isOnline()) {
                        Messenger.sendf(p,
                                "&eYou are running an outdated version of M0-CoreCord." +
                                        "\n&eLatest version: &6" + ver + ", &eyou are using: &6" + curr +
                                        "\n&eYou can download the latest version on Spigot:" +
                                        "\n&e" + plugin.getSpigotLink());
                    }
                });
            }
        }, 60L);
    }
}
