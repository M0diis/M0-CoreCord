package me.m0dii.CoreCord.Listeners;

import me.m0dii.CoreCord.Config;
import me.m0dii.CoreCord.CoreCord;
import me.m0dii.CoreCord.UpdateChecker;
import me.m0dii.CoreCord.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener
{
    private final CoreCord plugin;
    private final Config cfg;
    
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
                    new UpdateChecker(this.plugin, 91863).getVersion(ver ->
                    {
                        String curr = this.plugin.getDescription().getVersion();
                        
                        if (!curr.equalsIgnoreCase(
                                ver.replace("v", "")))
                        {
                            p.sendMessage(Utils.format("&eYou are running an outdated version of M0-CoreCord." +
                                    "\n&eLatest version: &6" + ver + ", &eyou are using: &6" + curr +
                                    "\n&eYou can download the latest version on Spigot:" +
                                    "\n&e" + plugin.getSpigotLink()));
                        }
                    });
                }
            }
        }, 50L);
    }
}
