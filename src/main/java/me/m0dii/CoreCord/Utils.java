package me.m0dii.CoreCord;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Utils
{
    private static SimpleDateFormat sdf;
    
    public static void setDateFormat(String format)
    {
        sdf = new SimpleDateFormat(format);
    }
    
    public static String getDateFromTimestamp(String timestamp)
    {
        Instant instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
        
        Date date = Date.from(instant);
        
        return sdf.format(date);
    }
    
    
}
