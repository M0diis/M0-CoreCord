package me.m0dii.CoreCord.Utils;

import me.m0dii.CoreCord.CoreCord;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Messenger
{
    private static final CoreCord plugin = CoreCord.getPlugin(CoreCord.class);
    
    public static void sendFormat(CommandSender s, String message)
    {
        s.sendMessage(format(message));
    }
    
    public static void sendFormatR(CommandSender s, String message, String what, Object to)
    {
        sendFormat(s, replace(message, what, to));
    }
    
    public static String replace(String in, String what, Object to)
    {
        return in.replace(what, String.valueOf(to));
    }
    
    public static String format(String text)
    {
        return ChatColor.translateAlternateColorCodes('&', text);
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
