package me.m0dii.CoreCord.Listeners;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import me.m0dii.CoreCord.CoSQL;
import me.m0dii.CoreCord.Utils.Config;
import me.m0dii.CoreCord.CoreCord;
import me.m0dii.CoreCord.Utils.Messenger;
import me.m0dii.CoreCord.Utils.Utils;
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
        
        Messenger.debug("Found message: " + e.getMessage().getContentRaw());
        Messenger.debug("Found prefix: " + cfg.getBotPrefix());
        
        if(!args[0].startsWith(cfg.getBotPrefix()))
        {
            Messenger.debug("Message not starting with prefix. Ignoring.");
            
            return;
        }
    
        String cmd = args[0].replace(cfg.getBotPrefix(), "");
        
        Member m = e.getMember();
        MessageChannel channel = e.getChannel();
        
        if(cfg.channelWhitelist())
            if(!cfg.getAllowedChannels().contains(channel.getId()))
                return;
            
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor("CoreCord")
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
            Messenger.debug("Member is null.");
            
            return;
        }
        
        boolean allowed = false;
        
        for(Role r : m.getRoles())
            if(allowedRoleIDS.contains(r.getId()))
                allowed = true;
            
        Messenger.info("User allowed to use commands: " + allowed);
    
        if(alias(cmd, "reload") && allowed)
        {
            plugin.reloadConfig();
        
            embed.setDescription("Configuration has been reloaded.");
            
            coSQL.connect();
        
            sendEmbed(channel, embed);
            
            Messenger.debug("Configuartion has been reloaded.");
            
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
            
            if(co == null)
            {
                Messenger.debug("CoreProtect instance was not found.");
                
                return;
            }
            
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
                Messenger.warn("Failed to connect to the database..");
                Messenger.debug(ex.getMessage());

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
                Messenger.debug("Executing [ " + cmd + " ] command by " +
                            "[ " + m.getUser().getAsTag() + " ] ");
    
                String time = getInfo(args, "t:", "time:")
                        .replace(",", "");
    
                if(time.length() == 0 || time.trim().isEmpty())
                {
                    embed.setDescription("Please specify time to lookup.");
        
                    sendEmbed(channel, embed);
        
                    return;
                }
                
                String user = clearSQL(getInfo(args, "u:", "user:"));
                
                String[] users = user.split(",");
    
                String action = getInfo(args, "a:", "action:");
                
                String[] actions = action.split(",");
    
                String block = getInfo(args, "b:", "block:");
    
                String[] blocks = block.split(",");
    
                String filter = getInfo(args, "f:", "filter:");
                
                List<String> filters = new ArrayList<>();
                
                if(filter.trim().length() != 0 && !filter.isEmpty())
                    filters = Arrays.asList(filter.trim().split(","));
                
                boolean reverse = Arrays.stream(args).anyMatch(arg ->
                        arg.equalsIgnoreCase("-r")
                     || arg.equalsIgnoreCase("-reverse"));
    
                Messenger.debug("User: " + user);
                Messenger.debug("Time: " + time);
                Messenger.debug("Action: " + action);
                Messenger.debug("Block: " + block);
                Messenger.debug("Filter: " + filter);
                Messenger.debug("Reverse: " + reverse);
                
                try
                {
                    List<String> results = coSQL.lookUpData(users, action, blocks, timeToSeconds(time));
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
                    
                    int filterMatches = 0;
                    
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
                                    filterMatches++;
                                    
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
    
                        if(cfg.showCount())
                            embed.setDescription("Found " + results.size() + " results.");
    
                        if(rows >= this.cfg.getRowsInPage())
                        {
                            embed.setFooter("Page " + (pages.size() + 1) + " • " +
                                    e.getAuthor().getAsTag());
                            
                            pages.add(new Page(PageType.EMBED, embed.build()));
            
                            embed = new EmbedBuilder()
                                    .setAuthor("CoreCord")
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
                        embed.setDescription(String.format("Found %d %s.", filterMatches, filterMatches == 1 ? "result" :
                                "results"));
                        
                        embed.clearFields();
        
                        sendEmbed(channel, embed);
        
                        return;
                    }
                    
                    if(filters.size() != 0 && filterMatches == 0)
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
                    Messenger.debug(ex.getMessage());
                    
                    Messenger.warn("SQL Exception has occurred..");
                    Messenger.warn("Attempting to reconnect.");
                    
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
        
            if(Utils.isDigit(String.valueOf(c)))
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
    
    private String getInfo(String[] args, String lu1, String lu2)
    {
        for(String arg : args)
            if(arg.startsWith(lu1) || arg.startsWith(lu2))
                return clean(arg, lu1, lu2);
        
        return "";
    }
    
    private String clean(String origin, String tr1, String tr2)
    {
        return origin.trim().replace(tr1, "").replace(tr2, "");
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
    
    private String clearSQL(String data)
    {
        return data.toLowerCase().replaceAll(
                "(select|where|group,by|order,by|left,join|union|co_)", "");
    }
}