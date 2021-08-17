package me.m0dii.CoreCord.Commands;

import me.m0dii.CoreCord.CoreCord;
import me.m0dii.CoreCord.Utils.Messenger;
import me.m0dii.CoreCord.Utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoreCo implements CommandExecutor
{
    private final CoreCord plugin;
    
    public CoreCo(CoreCord plugin)
    {
        this.plugin = plugin;
    }
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String alias, @NotNull String[] args)
    {
        if(sender instanceof ConsoleCommandSender)
        {
            if(args.length == 1)
            {
                if(args[0].equalsIgnoreCase("reload"))
                {
                    this.plugin.getCfg().reload(this.plugin);
    
                    sender.sendMessage("Configuration has been reloaded");
                }
            }
        }
        
        if(sender instanceof Player)
        {
            Player p = (Player)sender;
    
            if(!p.hasPermission("corecord.main"))
                return true;
    
            if(args.length == 1 && p.hasPermission(""))
            {
                if(canUse(args[0], "reload", "corecord.command.reload", p))
                {
                    this.plugin.getCfg().reload(this.plugin);
                    
                    Messenger.sendFormat(p, "&aConfiguration has been reloaded.");
                }
    
                if(canUse(args[0], "version", "corecord.command.version", p))
                {
                    Messenger.sendFormat(p, "&aYou are using CoreCord version &2" +
                            plugin.getDescription().getVersion());
                }
            }
        }
        
        return true;
    }
    
    private static boolean canUse(String arg, String cmd, String perm, Player pl)
    {
        return arg.equalsIgnoreCase(cmd) && pl.hasPermission(perm);
    }
}