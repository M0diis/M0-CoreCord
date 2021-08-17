package me.m0dii.CoreCord.Utils;

import me.m0dii.CoreCord.CoreCord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Messenger
{
    private static final CoreCord plugin = CoreCord.instance;
    
    public static void sendf(CommandSender s, String message)
    {
        s.sendMessage(format(message));
    }
    
    public static void sendfr(CommandSender s, String message, String what, Object to)
    {
        sendf(s, replace(message, what, to));
    }
    
    public static String replace(String in, String what, Object to)
    {
        return in.replace(what, String.valueOf(to));
    }
    
    public static String format(String text)
    {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static void debug(String msg)
    {
        if(plugin.getCfg().debugEnabled())
        {
            String prefix = "&3[&bCoreCord - DEBUG&3]&r ";
    
            Bukkit.getConsoleSender().sendMessage(format(prefix + msg));
        }
    }
    
    public static void info(String msg)
    {
        plugin.getLogger().info(msg);
    }
    
    public static void warn(String msg)
    {
        plugin.getLogger().warning(msg);
    }
}
