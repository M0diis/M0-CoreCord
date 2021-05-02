package me.m0dii.CoreCord.Listeners;

import me.m0dii.CoreCord.Config;
import me.m0dii.CoreCord.CoreCord;
import me.m0dii.CoreCord.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener
{
    private final CoreCord plugin;
    private final Config cfg;
    private final String latestVersion = "1.4";
    
    public PlayerJoin(CoreCord plugin)
    {
        this.cfg = plugin.getCfg();
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () ->
        {
            Player p = e.getPlayer();
            
            if(cfg.notifyUpdate())
            {
                if(p.hasPermission("corecord.update.notify"))
                {
                    String currentVersion = plugin.getDescription().getVersion();
            
                    if(!currentVersion.equalsIgnoreCase(latestVersion))
                    {
                        p.sendMessage(Utils.format("&eYou are running an outdated version of M0-CoreCord." +
                                "\n&eYou can download the latest version on Spigot:" +
                                "\n&e" + plugin.getSpigotLink()));
                    }
                }
            }
        }, 50L);
    }
}
