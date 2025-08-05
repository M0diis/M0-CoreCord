package me.m0dii.corecord.utils;

import me.m0dii.corecord.CoreCord;
import org.bukkit.Bukkit;
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
        this.msgs.add(Utils.format(msg));

        return this;
    }

    public void send() {
        for (String msg : msgs) {
            this.receiver.sendMessage(msg);
        }
    }

    public static void sendf(CommandSender s, String message) {
        s.sendMessage(Utils.format(message));
    }

    public static String replace(String in, String what, Object to) {
        return in.replace(what, String.valueOf(to));
    }

    public static void debug(String msg) {
        if (plugin.getCfg().isDebugEnabled()) {
            String prefix = "&3[&bCoreCord - DEBUG&3]&r ";

            Bukkit.getConsoleSender().sendMessage(Utils.format(prefix + msg));
        }
    }

    public static void info(String msg) {
        String prefix = "&2[&aCoreCord - INFO&2]&r ";

        Bukkit.getConsoleSender().sendMessage(Utils.format(prefix + msg));
    }

    public static void warn(String msg) {
        String prefix = "&6[&eCoreCord - WARN&6]&r ";

        Bukkit.getConsoleSender().sendMessage(Utils.format(prefix + msg));
    }
}
