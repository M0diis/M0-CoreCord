package me.m0dii.corecord.utils;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WebhookLogger
{
    private WebhookClient client;
    
    private String channelID;
    
    private List<String> actions;
    
    public WebhookLogger(String url, String channelID)
    {
        this.channelID = channelID;
        
        this.actions = new ArrayList<>();
        
        setUp(url);
    }
    
    private void setUp(String url)
    {
        WebhookClientBuilder builder = new WebhookClientBuilder(url);
        
        setThreadFactory(builder);
        
        client = builder.build();
    }
    
    public void send(String username, String msg)
    {
        client.send(getMsgBuilder(username, msg).build());
    }
    
    public void close()
    {
        client.close();
    }
    
    public boolean hasAction(String action)
    {
        return actions.contains(action);
    }
    
    public void addActions(List<String> actions)
    {
        this.actions.addAll(actions);
    }
    
    public void addAction(String action)
    {
        actions.add(action);
    }
    
    public WebhookMessageBuilder getMsgBuilder(String username, @Nullable String msg)
    {
        WebhookMessageBuilder builder =
                new WebhookMessageBuilder().setAvatarUrl("https://minotar.net/avatar/" + username)
                        .setUsername(username);
        
        if(msg != null) builder.setContent(msg.trim());
        
        return builder;
    }
    
    public WebhookMessageBuilder getMsgBuilder(String username, WebhookEmbedBuilder embed)
    {
        WebhookMessageBuilder builder =
                new WebhookMessageBuilder().setAvatarUrl("https://minotar.net/avatar/" + username)
                        .setUsername(username);
        
        if(embed != null) builder.addEmbeds(embed.build());
        
        return builder;
    }
    
    
    public void setThreadFactory(WebhookClientBuilder builder)
    {
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setDaemon(true);
            return thread;
        }).setWait(true);
    }
    
    public WebhookClient getClient(String webhookUrl)
    {
        WebhookClientBuilder builder = new WebhookClientBuilder(webhookUrl);
        
        setThreadFactory(builder);
        
        return builder.build();
    }
}
