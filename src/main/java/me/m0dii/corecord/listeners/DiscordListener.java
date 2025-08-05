package me.m0dii.corecord.listeners;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.InteractPage;
import com.github.ygimenez.model.Page;
import me.clip.placeholderapi.PlaceholderAPI;
import me.m0dii.corecord.CoSQL;
import me.m0dii.corecord.CoreCord;
import me.m0dii.corecord.utils.Config;
import me.m0dii.corecord.utils.Message;
import me.m0dii.corecord.utils.Messenger;
import me.m0dii.corecord.utils.Utils;
import net.coreprotect.CoreProtect;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter {
    private final boolean usePAPI;

    private final CoSQL coSQL;
    private final CoreCord plugin;
    private final Config cfg;

    public DiscordListener(@NotNull CoreCord plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getCfg();
        this.coSQL = plugin.getCoSQL();

        this.usePAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String[] args = e.getMessage().getContentRaw().split(" ");

        Messenger.debug("Found message: " + e.getMessage().getContentRaw());
        Messenger.debug("Found prefix: " + cfg.getBotPrefix());
        Messenger.debug("Arg 0: " + args[0]);

        if (!args[0].startsWith(cfg.getBotPrefix())) {
            Messenger.debug("Message not starting with prefix. Ignoring.");

            return;
        }

        String cmd = args[0].replace(cfg.getBotPrefix(), "");

        Member m = e.getMember();
        MessageChannel channel = e.getChannel();

        if (cfg.isChannelWhitelist()) {
            if (!cfg.getAllowedChannels().contains(channel.getId())) {
                return;
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(PlaceholderAPI.setPlaceholders(null, plugin.getCfg().getEmbedTitle()))
                .setFooter(e.getAuthor().getAsTag(), null)
                .setColor(Color.decode(plugin.getCfg().getEmbedColor()));

        if (alias(cmd, "ping")) {
            embed.setDescription("Pong");

            sendEmbed(channel, embed);

            return;
        }

        List<String> allowedRoleIDS = cfg.getAllowedRoles();

        if (m == null) {
            Messenger.debug("Member is null.");

            return;
        }

        boolean allowed = m.getRoles().stream().anyMatch(r -> allowedRoleIDS.contains(r.getId()));

        Messenger.debug("User allowed to use commands: " + allowed);

        if (alias(cmd, "reload") && allowed) {
            plugin.getCfg().reload(this.plugin);

            embed.setDescription(cfg.getMessage(Message.EMBED_CONFIG_RELOAD));

            coSQL.connect();

            sendEmbed(channel, embed);

            Messenger.debug("Configuartion has been reloaded.");

            return;
        }

        if (cmd.isBlank() || alias(cmd, "help, commands") && allowed) {
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

        if (alias(cmd, "version, ver") && allowed) {
            CoreProtect co = CoreProtect.getInstance();

            if (co == null) {
                Messenger.debug("CoreProtect instance was not found.");

                return;
            }

            embed.addField("CoreProtect", co.getPluginMeta().getVersion(), false)
                    .addField("CoreCord", plugin.getPluginMeta().getVersion(), false)
                    .addField("Server", plugin.getServer().getVersion(), false)
                    .addField("OS", System.getProperty("os.name"), false)
                    .addField("Bukkit", plugin.getServer().getBukkitVersion(), false);

            sendEmbed(channel, embed);

            return;
        }

        if (alias(cmd, "testconnection") && allowed) {
            try {
                boolean connected = !CoSQL.connection.isClosed();

                if (connected) {
                    embed.setDescription("Connection is established successfully.");
                } else {
                    embed.setDescription("Connection has not been found. Reconnecting..");

                    coSQL.connect();
                }

                sendEmbed(channel, embed);

                return;
            } catch (SQLException ex) {
                Messenger.warn("Failed to connect to the database..");
                Messenger.debug(ex.getMessage());

                embed.setDescription("Cannot find a connection to the database, trying to reconnect..");

                coSQL.connect();

                sendEmbed(channel, embed);

                return;
            }
        }

        if (args.length >= 2 && allowed) {
            if (alias(cmd, "lookup, lu, l")) {
                Messenger.debug("Executing [ " + cmd + " ] command by " +
                        "[ " + m.getUser().getAsTag() + " ] ");

                String time = getInfo(args, "t:", "time:")
                        .replace(",", "");

                if (time.isEmpty() || time.trim().isEmpty()) {
                    embed.setDescription(cfg.getMessage(Message.EMBED_SPECIFY_TIME));

                    sendEmbed(channel, embed);

                    return;
                }

                String user = clearSQL(getInfo(args, "u:", "user:"));

                if (user.isBlank()) {
                    user = clearSQL(args[1]);
                }

                String[] users = user.split(",");

                String action = getInfo(args, "a:", "action:");

                String[] actions = action.split(",");

                String block = getInfo(args, "b:", "block:");

                String[] blocks = block.split(",");

                String filter = getInfo(args, "f:", "filter:");

                if (filter.isBlank()) {
                    filter = getInfo(args, "include:", "contains:");
                }

                List<String> filters = new ArrayList<>();

                if (!filter.trim().isEmpty() && !filter.isEmpty()) {
                    filters = Arrays.asList(filter.trim().split(","));
                }

                boolean reverse = Arrays.stream(args).noneMatch(arg ->
                        arg.equalsIgnoreCase("-r")
                                || arg.equalsIgnoreCase("-reverse"));

                boolean file = Arrays.stream(args).anyMatch(arg ->
                        arg.equalsIgnoreCase("-f")
                                || arg.equalsIgnoreCase("-file"));

                boolean hasCustomRows = Arrays.stream(args).anyMatch(arg ->
                        arg.startsWith("-r=")
                                || arg.startsWith("-rows="));

                boolean showCount = Arrays.stream(args).anyMatch(arg ->
                        arg.equalsIgnoreCase("#count"));

                int rowsPerPage = cfg.getRowsInPage();

                if (hasCustomRows) {
                    Optional<String> customRowsOpt = Arrays.stream(args)
                            .filter(arg -> arg.startsWith("-r=") || arg.startsWith("-rows="))
                            .findFirst();

                    if (customRowsOpt.isPresent()) {
                        String customRows = customRowsOpt.get();

                        String[] split = customRows.split("=");

                        if (split.length == 2) {
                            try {
                                rowsPerPage = Integer.parseInt(split[1]);
                            } catch (NumberFormatException ex) {
                                Messenger.warn("Failed to parse custom rows per page.");
                                Messenger.debug(ex.getMessage());
                            }
                        }
                    }
                }

                Messenger.debug("User: " + user);
                Messenger.debug("Time: " + time);
                Messenger.debug("Action: " + action);
                Messenger.debug("Block: " + block);
                Messenger.debug("Filter: " + filter);
                Messenger.debug("Reverse: " + reverse);

                if (action.isBlank()) {
                    action = "all";
                }

                try {
                    List<String> results = coSQL.lookUpData(users, action, blocks, timeToSeconds(time));

                    if (file) {
                        outputFile(results, channel);

                        return;
                    }

                    List<Page> pages = new ArrayList<>();

                    int rows = 0;

                    if (results.isEmpty()) {
                        String msg = PlaceholderAPI.setPlaceholders(null,
                                plugin.getCfg().getMessage(Message.EMBED_NO_RESULTS));

                        embed.setDescription(msg);

                        sendEmbed(channel, embed);

                        return;
                    }

                    if (filters.isEmpty() && showCount) {
                        String msg = PlaceholderAPI.setPlaceholders(null,
                                plugin.getCfg().getMessage(Message.EMBED_RESULT_COUNT)
                                        .replace("%count%", String.valueOf(results.size())));

                        embed.setDescription(msg);

                        sendEmbed(channel, embed);

                        return;
                    }

                    int filterMatches = 0;

                    for (int i = reverse ? results.size() - 1 : 0; reverse ? i >= 0 : i < results.size(); ) {
                        String[] values = results.get(i).split(" \\| ");

                        String date = values[0];
                        String data = values[1];

                        String xyz = data.split("\n")[0];
                        String value = data.split("\n")[1];

                        if (cfg.getExcludedData().stream()
                                .anyMatch(s -> StringUtils.containsIgnoreCase(value, s))) {
                            if (reverse) i--;
                            else i++;

                            continue;
                        }

                        String[] xyzSplit = xyz.split(" ");

                        String x = xyzSplit[0].replace("X:", "");
                        String y = xyzSplit[1].replace("Y:", "");
                        String z = xyzSplit[2].replace("Z:", "");

                        String xyzRow = setPlaceholders(cfg.getMessage(Message.COORDINATE_ROW))
                                .replace("%x%", x)
                                .replace("%y%", y)
                                .replace("%z%", z);

                        Messenger.debug("XYZ: " + xyzRow);
                        Messenger.debug("Value: " + value);

                        data = xyzRow + "\n" + value;

                        if (!filters.isEmpty()) {
                            String tempFilter = data.split("\n")[1].replace("/", "");
                            String[] split = tempFilter.split(" ");

                            boolean skip = true;

                            for (String sp : split) {
                                if (filters.contains(sp.trim())) {
                                    filterMatches++;

                                    skip = false;
                                }
                            }

                            if (skip) {
                                if (reverse) i--;
                                else i++;

                                continue;
                            }
                        }

                        embed.addField(date, data, false);

                        rows++;

                        if (cfg.isShowCount()) {
                            if (!filters.isEmpty()) {
                                embed.setDescription(String.format("Found %d %s.",
                                        filterMatches, filterMatches == 1 ? "result" : "results"));
                            } else {
                                String msg = PlaceholderAPI.setPlaceholders(null,
                                        plugin.getCfg().getMessage(Message.EMBED_RESULT_COUNT)
                                                .replace("%count%", String.valueOf(results.size())));

                                embed.setDescription(msg);
                            }
                        }

                        if (rows >= rowsPerPage) {
                            String footer = setPlaceholders(cfg.getEmbedFooter())
                                    .replace("%page%", String.valueOf(pages.size() + 1))
                                    .replace("%message_author_tag%", e.getAuthor().getAsTag())
                                    .replace("%message_author_name%", e.getMember().getEffectiveName())
                                    .replace("%message_member_nickname%", e.getMember().getNickname());

                            embed.setFooter(footer);

                            pages.add(InteractPage.of(embed.build()));

                            embed = new EmbedBuilder()
                                    .setAuthor(setPlaceholders(cfg.getMessage(Message.EMBED_TITLE)))
                                    .setColor(Color.decode(cfg.getEmbedColor()));

                            rows = 0;
                        }

                        if (reverse) i--;
                        else i++;
                    }

                    if (!filters.isEmpty() && showCount) {
                        embed.setDescription(String.format("Found %d %s.",
                                filterMatches, filterMatches == 1 ? "result" : "results"));

                        embed.clearFields();

                        sendEmbed(channel, embed);

                        return;
                    }

                    if (!filters.isEmpty() && filterMatches == 0) {
                        embed.setDescription(setPlaceholders(cfg.getMessage(Message.EMBED_NO_RESULTS_FILTER)));

                        sendEmbed(channel, embed);

                        return;
                    }

                    if (pages.isEmpty()) {
                        pages.add(InteractPage.of(embed.build()));
                    }

                    channel.sendMessageEmbeds((MessageEmbed) pages.getFirst().getContent())
                            .queue(success -> Pages.paginate(success, pages, true));
                } catch (SQLException ex) {
                    Messenger.debug(ex.getMessage());

                    Messenger.warn("SQL Exception has occurred..");
                    Messenger.warn("Attempting to reconnect..");

                    coSQL.connect();
                }
            }
        }
    }

    private void outputFile(List<String> results, MessageChannel channel) {
        if (results.isEmpty()) {
            channel.sendMessage(setPlaceholders(
                    plugin.getCfg().getMessage(Message.EMBED_NO_RESULTS))).queue();

            return;
        }

        List<String> excludes = cfg.getExcludedData();

        String lines = results.stream()
                .filter(s -> excludes.stream().noneMatch(s::contains))
                .collect(Collectors.joining("\n"));

        try (FileUpload fileUpload = FileUpload.fromData(lines.getBytes(), "results.txt")) {
            channel.sendFiles(fileUpload).queue();
        } catch (Exception ex) {
            Messenger.warn("Failed to create file upload: " + ex.getMessage());
            channel.sendMessage("An error occurred while creating the results file.").queue();
        }
    }

    private long timeToSeconds(String time) {
        long total = 0;

        String tempDigit = "";

        for (int i = 0; i < time.length(); i++) {
            char c = time.charAt(i);

            if (Utils.isDigit(String.valueOf(c))) {
                tempDigit += c;
            } else {
                total += getSecondsFromTime(tempDigit, String.valueOf(c));

                tempDigit = "";
            }
        }

        return total < 0 ? 0 : total;
    }

    private int getSecondsFromTime(String digit, String type) {
        int total = 0;

        int num = Integer.parseInt(digit);

        switch (type.toLowerCase()) {
            case "s", "sec", "seconds" -> total += num;
            case "m", "min", "minutes" -> total += num * 60;
            case "h", "hour", "hours" -> total += num * 3600;
            case "d", "day", "days" -> total += num * 86400;
            case "w", "week", "weeks" -> total += num * 604800;
            default -> {
            }
        }

        return total;
    }

    private String getInfo(String[] args, String lu1, String lu2) {
        return Arrays.stream(args).filter(arg -> arg.startsWith(lu1)
                        || arg.startsWith(lu2)).findFirst().map(arg -> clean(arg, lu1, lu2))
                .orElse("");
    }

    private String setPlaceholders(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return usePAPI ? PlaceholderAPI.setPlaceholders(null, text) : text;
    }

    private String clean(String origin, String tr1, String tr2) {
        return origin.trim().replace(tr1, "").replace(tr2, "");
    }

    private void sendEmbed(@NotNull MessageChannel ch, @NotNull EmbedBuilder embed) {
        ch.sendMessageEmbeds(embed.build()).queue();
    }

    private boolean alias(@NotNull String cmd, @NotNull String names) {
        return Arrays.stream(names.split(", ")).anyMatch(cmd::equalsIgnoreCase);
    }

    private String clearSQL(@NotNull String data) {
        return data.toLowerCase().replaceAll(
                "\\b(select|where|group by|order by|left join|union|co_)\\b", ""
        );
    }
}