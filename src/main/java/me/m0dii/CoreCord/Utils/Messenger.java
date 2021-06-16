package me.m0dii.CoreCord.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Messenger
{
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
    
}
