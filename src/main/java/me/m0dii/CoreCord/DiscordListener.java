package me.m0dii.CoreCord;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter
{
    private final MySQL coSQL;
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
        
        if(!args[0].startsWith(cfg.getBotPrefix())) return;
    
        String cmd = args[0].replace(cfg.getBotPrefix(), "");
        
        Member m = e.getMember();
        MessageChannel channel = e.getChannel();
        EmbedBuilder embed = new EmbedBuilder();
    
        embed.setAuthor("CoreCord")
                .setFooter(e.getAuthor().getAsTag(), null)
                .setColor(Color.CYAN);
        
        List<String> allowedRoleIDS = cfg.getAllowedRoles();
        
        if(m == null)
            return;
        
        boolean allowed = false;
        
        for(Role r : m.getRoles())
            if(allowedRoleIDS.contains(r.getId()))
                allowed = true;
            
        if(args.length >= 2 && allowed)
        {
            if(alias(cmd, "lookup, lu"))
            {
                String user = "";
                
                for(String arg : args)
                    if(arg.startsWith("u:") || arg.startsWith("user:"))
                        user = arg;
            
                user = user.replace("u:", "");
                user = user.replace("user:", "");
                
                String[] users = user.split(",");
    
                String action = "";
    
                for(String arg : args)
                    if(arg.startsWith("a:") || arg.startsWith("action:"))
                        action = arg;
    
                action = action.replace("a:", "");
                action = action.replace("action:", "");
    
                String[] actions = action.split(",");
    
                String time = "";
    
                for(String arg : args)
                    if(arg.startsWith("t:") || arg.startsWith("time:"))
                        time = arg;
    
                time = time.replace("t:", "");
                time = time.replace("time:", "");
                
                try
                {
                    List<String> results = coSQL.lookUpData(user, action, timeToSeconds(time));
                    ArrayList<Page> pages = new ArrayList<>();
                    
                    int counter = 0;
    
                    if(results.size() == 0)
                    {
                        embed.setDescription("No results found.");
    
                        sendMsg(channel, embed);
                        
                        return;
                    }
    
                    for(int i = results.size() - 1; i >= 0; i--)
                    {
                        String r = results.get(i);
                        String[] split = r.split(" \\| ");
        
                        String date = split[0];
                        String values = split[1];
        
                        embed.addField(date, values, false);
        
                        counter++;
        
                        if(counter >= 5)
                        {
                            pages.add(new Page(PageType.EMBED, embed.build()));
            
                            embed = new EmbedBuilder();
            
                            embed.setAuthor("CoreCord")
                                    .setFooter(e.getAuthor().getAsTag(), null)
                                    .setColor(Color.CYAN);
            
                            counter = 0;
                        }
                    }
                    
                    if(pages.size() == 0)
                        pages.add(new Page(PageType.EMBED, embed.build()));
                    
                    channel.sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                        Pages.paginate(success, pages);
                    });
                    
                    //sendMsg(channel, embed);
                }
                catch(SQLException ex)
                {
                    ex.printStackTrace();
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
        
        return total;
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
            case "s":
                total += num;
                break;
                
            case "m":
                total += num * 60;
                break;
                
            case "h":
                total += num * 3600;
                break;
                
            case "d":
                total += num * 86400;
                break;
                
            default:
                break;
        }
    
        return total;
    }
    
    
    void sendMsg(MessageChannel ch, EmbedBuilder embed)
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