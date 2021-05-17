package me.m0dii.CoreCord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordCMD
{
    private MessageReceivedEvent e;
    
    public Member member;
    
    public String cmd;
    
    public String[] args;
    
    public DiscordCMD(MessageReceivedEvent e, String botPrefix)
    {
        this.member = e.getMember();
    
        List<String> tempArgs = new ArrayList<>(
                Arrays.asList(e.getMessage().getContentRaw().split(" ")));
    
        this.cmd = tempArgs.remove(0);
    
        this.args = tempArgs.toArray(new String[0]);
    }
    
}
