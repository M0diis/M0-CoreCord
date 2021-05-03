package me.m0dii.CoreCord;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.coreprotect.CoreProtect;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordListener extends ListenerAdapter
{
    private final CoSQL coSQL;
    private final CoreCord plugin;
    private final Config cfg;
    
    public DiscordListener(CoreCord plugin)
    {
        this.plugin = plugin;
        this.cfg = plugin.getCfg();
        this.coSQL = plugin.getCoSQL();
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent e)
    {
        String[] args = e.getMessage().getContentRaw().split(" ");
    
        boolean debug = cfg.debugEnabled();
        
        if(debug)
        {
            plugin.getLogger().info("Message: " + e.getMessage().getContentRaw());
            
            plugin.getLogger().info("Prefix: " + cfg.getBotPrefix());
        }
        
        if(!args[0].startsWith(cfg.getBotPrefix()))
        {
            if(debug)
                plugin.getLogger().info("Message not starting with prefix. Ignoring.");
            
            return;
        }
    
        String cmd = args[0].replace(cfg.getBotPrefix(), "");
        
        Member m = e.getMember();
        MessageChannel channel = e.getChannel();
        
        if(cfg.channelWhitelist())
            if(!cfg.getAllowedChannels().contains(channel.getId()))
                return;
            
        EmbedBuilder embed = new EmbedBuilder();
    
        embed.setAuthor("CoreCord")
                .setFooter(e.getAuthor().getAsTag(), null)
                .setColor(Color.CYAN);
        
        if(alias(cmd, "ping"))
        {
            embed.setDescription("Pong");
            
            sendEmbed(channel, embed);
            
            return;
        }
        
        List<String> allowedRoleIDS = cfg.getAllowedRoles();
        
        if(m == null)
        {
            if(debug)
                plugin.getLogger().info("Member is null.");
            
            return;
        }
        
        boolean allowed = false;
        
        for(Role r : m.getRoles())
            if(allowedRoleIDS.contains(r.getId()))
                allowed = true;
    
        if(debug)
            plugin.getLogger().info("User allowed to use commands: " + allowed);
    
        if(alias(cmd, "reload") && allowed)
        {
            this.cfg.reload(this.plugin);
        
            embed.setDescription("Configuration has been reloaded.");
            
            coSQL.connect();
        
            sendEmbed(channel, embed);
            
            return;
        }
        
        if(alias(cmd, "help, commands") && allowed)
        {
            String pr = cfg.getBotPrefix();
            
            embed.setDescription("\nBOT Prefix: **" + pr + "**")
                    .addField("Lookup", pr + "lookup u:user a:action t:time", false)
                    .addField("Filter", pr + "lookup u:user a:action t:time f:filter", false)
                    .addField("Reverse", pr + "lookup u:user a:action t:time -r", false)
                    .addField("Count", pr + "lookup u:user a:action t:time #count", false)
                    .addField("Reload", pr + "reload", false)
                    .addField("Actions", "block, command, chat, drop, session, container", false)
                    .addField("Time", "ex.: 1m; 1d12h; 1h1m1s; 5h,30s; 23h,30m,60s; etc.", false);
            
            sendEmbed(channel, embed);
            
            return;
        }
        
        if(alias(cmd, "version, ver") && allowed)
        {
            CoreProtect co = CoreProtect.getInstance();
            
            embed.addField("CoreProtect", co.getDescription().getVersion(), false)
                    .addField("CoreCord", plugin.getDescription().getVersion(), false)
                    .addField("Server", plugin.getServer().getVersion(), false)
                    .addField("OS", System.getProperty("os.name"), false)
                    .addField("Bukkit", plugin.getServer().getBukkitVersion(), false);
            
            sendEmbed(channel, embed);
            
            return;
        }
    
        if(alias(cmd, "testconnection") && allowed)
        {
            try
            {
                boolean connected = !CoSQL.connection.isClosed();
                
                if(connected)
                {
                    embed.setDescription("Connection is established successfully.");
    
                }
                else
                {
                    embed.setDescription("Connection has not been found. Reconnecting..");
    
                    coSQL.connect();
                }
                
                sendEmbed(channel, embed);
                
                return;
            }
            catch(SQLException ex)
            {
                if(debug)
                    ex.printStackTrace();
    
                embed.setDescription("Cannot find a connection to the database, reconnecting..");
    
                coSQL.connect();
    
                sendEmbed(channel, embed);
                
                return;
            }
        }
            
        if(args.length >= 2 && allowed)
        {
            if(alias(cmd, "lookup, lu, l"))
            {
                if(debug)
                    plugin.getLogger().info("Executing [ " + cmd + " ] command by " +
                            "[ " + m.getUser().getAsTag() + " ] ");
    
                String time = "";
    
                for(String arg : args)
                    if(arg.startsWith("t:") || arg.startsWith("time:"))
                    {
                        time = arg;
                        
                        break;
                    }
    
                time = time.replace("t:", "")
                        .replace("time:", "")
                        .replace(",", "");
    
                if(time.length() == 0 || time.trim().isEmpty())
                {
                    embed.setDescription("Please specify time to lookup.");
        
                    sendEmbed(channel, embed);
        
                    return;
                }
                
                String user = "";
                
                for(String arg : args)
                    if(arg.startsWith("u:") || arg.startsWith("user:"))
                    {
                        user = arg;
                        
                        break;
                    }
            
                user = user.replace("u:", "")
                    .replace("user:", "");
                
                String[] users = user.split(",");
    
                String action = "";
    
                for(String arg : args)
                    if(arg.startsWith("a:") || arg.startsWith("action:"))
                    {
                        action = arg;
                        
                        break;
                    }
    
                action = action.replace("a:", "")
                    .replace("action:", "");
    
                String[] actions = action.split(",");
                
                String filter = "";
    
                for(String arg : args)
                    if(arg.startsWith("f:") || arg.startsWith("filter:"))
                    {
                        filter = arg;
                        
                        break;
                    }
                    
                filter = filter.trim().replace("f:", "")
                        .replace("filter:", "");
                    
                List<String> filters = new ArrayList<>();
                
                if(filter.trim().length() != 0 && !filter.isEmpty())
                    filters = Arrays.asList(filter.trim().split(","));
                
                boolean reverse = false;
                
                for(String arg : args)
                    if(arg.equalsIgnoreCase("-r")
                        || arg.equalsIgnoreCase("-reverse"))
                    {
                        reverse = true;
                        
                        break;
                    }
                
                if(debug)
                {
                    plugin.getLogger().info("User: " + user);
                    plugin.getLogger().info("Time: " + time);
                    plugin.getLogger().info("Action: " + action);
                    plugin.getLogger().info("Filter: " + filter);
                    plugin.getLogger().info("Reverse: " + reverse);
                }
                
                try
                {
                    List<String> results = coSQL.lookUpData(user, action, timeToSeconds(time));
                    ArrayList<Page> pages = new ArrayList<>();
                    
                    int rows = 0;
    
                    if(results.size() == 0)
                    {
                        embed.setDescription("No results found.");
    
                        sendEmbed(channel, embed);
                        
                        return;
                    }
                    
                    if(filters.size() == 0 &&
                            args[args.length - 1].equalsIgnoreCase("#count"))
                    {
                        embed.setDescription("Found " + results.size() + " results.");
                        
                        sendEmbed(channel, embed);
                        
                        return;
                    }
                    
                    int filterMacthes = 0;
                    
                    for(int i = reverse ? results.size() - 1 : 0; reverse ?  i >= 0 : i < results.size();)
                    {
                        String[] values = results.get(i).split(" \\| ");
        
                        String date = values[0];
                        String data = values[1];
                        
                        if(filters.size() != 0)
                        {
                            String tempFilter = data.split("\n")[1].replace("/", "");
                            String[] split = tempFilter.split(" ");
    
                            boolean skip = true;
                            
                            for(String sp : split)
                            {
                                if(filters.contains(sp.trim()))
                                {
                                    filterMacthes++;
                                    
                                    skip = false;
                                }
                            }
                            
                            if(skip)
                            {
                                if(reverse)
                                    i--;
                                else i++;
        
                                continue;
                            }
                        }
        
                        embed.addField(date, data, false);
        
                        rows++;
        
                        if(rows >= this.cfg.getRowsInPage())
                        {
                            if(cfg.showCount())
                                embed.setDescription("Found " + results.size() + " results.");
    
                            embed.setFooter("Page " + (pages.size() + 1) + " • " +
                                    e.getAuthor().getAsTag());
                            
                            pages.add(new Page(PageType.EMBED, embed.build()));
            
                            embed = new EmbedBuilder();
            
                            embed.setAuthor("CoreCord")
                                    .setColor(Color.CYAN);
            
                            rows = 0;
                        }
    
                        if(reverse)
                            i--;
                        else i++;
                    }
    
                    if(filters.size() != 0 &&
                            args[args.length - 1].equalsIgnoreCase("#count"))
                    {
                        embed.setDescription(String.format("Found %d %s.", filterMacthes, filterMacthes == 1 ? "result" :
                                "results"));
                        
                        embed.clearFields();
        
                        sendEmbed(channel, embed);
        
                        return;
                    }
                    
                    if(filters.size() != 0 && filterMacthes == 0)
                    {
                        embed.setDescription("No results found by specified filter.");
    
                        sendEmbed(channel, embed);
    
                        return;
                    }
                    
                    if(pages.size() == 0)
                        pages.add(new Page(PageType.EMBED, embed.build()));
                    
                    channel.sendMessage((MessageEmbed) pages.get(0).getContent())
                            .queue(success -> Pages.paginate(success, pages));
                }
                catch(SQLException ex)
                {
                    if(debug)
                        ex.printStackTrace();
                    
                    plugin.getLogger().warning("SQL Exception has occurred..");
                    plugin.getLogger().warning("Attempting to reconnect..");
                    
                    coSQL.connect();
                }
            }
        }
    }
    
    private long timeToSeconds(String time)
    {
        long total = 0;
        
        String tempDigit = "";
        
        for (int i = 0; i < time.length(); i++)
        {
            char c = time.charAt(i);
        
            if(isDigit(String.valueOf(c)))
            {
                tempDigit += c;
            }
            else
            {
                total += getSecondsFromTime(tempDigit, String.valueOf(c));
                
                tempDigit = "";
            }
        }
    
        return total < 0 ? 0 : total;
    }
    
    private boolean isDigit(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
    
    private int getSecondsFromTime(String digit, String type)
    {
        int total = 0;
    
        int num = Integer.parseInt(digit);
    
        switch(type.toLowerCase())
        {
            case "s": case "sec": case "seconds":
                total += num;
                break;
                
            case "m": case "min": case "minutes":
                total += num * 60;
                break;
                
            case "h": case "hour": case "hours":
                total += num * 3600;
                break;
                
            case "d": case "day": case "days":
                total += num * 86400;
                break;
    
            case "w": case "week": case "weeks":
                total += num * 604800;
                break;
                
            default:
                break;
        }
    
        return total;
    }
    
    private void sendEmbed(MessageChannel ch, EmbedBuilder embed)
    {
        ch.sendMessage(embed.build()).queue();
    }
    
    private boolean alias(String cmd, String names)
    {
        String[] split = names.split(", ");
        
        for(String s : split)
        {
            if(cmd.equalsIgnoreCase(s))
                return true;
        }
        
        return false;
    }
}