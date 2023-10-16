package me.m0dii.corecord.utils;

import me.m0dii.corecord.CoreCord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Messenger {
    private static final CoreCord plugin = CoreCord.getInstance();

    private final CommandSender receiver;
    private final List<String> msgs;

    public Messenger(CommandSender receiver) {
        this.receiver = receiver;
        this.msgs = new ArrayList<>();
    }

    public Messenger add(String msg) {
        this.msgs.add(format(msg));

        return this;
    }

    public void send() {
        for (String msg : msgs) {
            this.receiver.sendMessage(msg);
        }
    }


    public Messenger clear() {
        this.msgs.clear();

        return this;
    }

    public static void sendf(CommandSender s, String message) {
        s.sendMessage(format(message));
    }

    public static void sendfr(CommandSender s, String message, String what, Object to) {
        sendf(s, replace(message, what, to));
    }

    public static String replace(String in, String what, Object to) {
        return in.replace(what, String.valueOf(to));
    }

    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void debug(String msg) {
        if (plugin.getCfg().isDebugEnabled()) {
            String prefix = "&3[&bCoreCord - DEBUG&3]&r ";

            Bukkit.getConsoleSender().sendMessage(format(prefix + msg));
        }
    }

    public static void info(String msg) {
        String prefix = "&2[&aCoreCord - INFO&2]&r ";

        Bukkit.getConsoleSender().sendMessage(format(prefix + msg));
    }

    public static void warn(String msg) {
        String prefix = "&6[&eCoreCord - WARN&6]&r ";

        Bukkit.getConsoleSender().sendMessage(format(prefix + msg));
    }
}
