package me.m0dii.corecord.commands;

import me.m0dii.corecord.CoreCord;
import me.m0dii.corecord.utils.Messenger;
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
        
        if(sender instanceof Player p)
        {
            if(!p.hasPermission("corecord.main"))
                return true;
    
            if(args.length == 1 && p.hasPermission(""))
            {
                if(allowedToUse(args, "reload", p))
                {
                    this.plugin.getCfg().reload(this.plugin);
    
                    Messenger.sendf(p, "&bConfiguration has been reloaded.");
                }
    
                if(allowedToUse(args, "version", p))
                {
                    Messenger.sendf(p, "&bYou are using CoreCord version &3" +
                            plugin.getDescription().getVersion());
                }
            }
        }
        
        return true;
    }
    
    private boolean allowedToUse(String[] args, String cmd, Player pl)
    {
        return args[0].equalsIgnoreCase(cmd) && pl.hasPermission("corecord.command." + cmd);
    }
}
