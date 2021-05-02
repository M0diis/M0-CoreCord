package me.m0dii.CoreCord;

import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Utils
{
    private static final CoreCord plugin = CoreCord.getPlugin(CoreCord.class);
    
    private static SimpleDateFormat sdf;
    
    public static void setDateFormat(String format)
    {
        sdf = new SimpleDateFormat(format);
    }
    
    public static String getDateFromTimestamp(String timestamp)
    {
        Instant instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
        
        Date date = Date.from(instant);
        
        if(sdf == null)
            sdf = new SimpleDateFormat(plugin.getCfg().getDateFormat());
        
        return sdf.format(date);
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
