package me.m0dii.corecord.utils;

import lombok.Getter;
import me.m0dii.corecord.CoreCord;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {
    private String cfgVersion;
    private boolean debugging = false;

    @Getter
    private boolean useMySQL;
    @Getter
    private String host;
    @Getter
    private String database;
    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private int port;

    private String embedLeft;
    private String embedRight;
    @Getter
    private String embedClose;
    private String embedColor;
    private String embedTitle;
    @Getter
    private String embedFooter;
    @Getter
    private boolean deleteOnClose;
    private int rowsInPage;

    @Getter
    private boolean showCount;

    @Getter
    private String dateFormat;

    @Getter
    private String botToken;
    @Getter
    private String botPrefix;

    @Getter
    private boolean channelWhitelist;

    @Getter
    private List<String> allowedRoles;
    @Getter
    private List<String> allowedChannels;

    @Getter
    private List<String> excludedData;

    @Getter
    private boolean notifyUpdate;

    private FileConfiguration cfg;

    private Map<Message, String> messages;

    public String getMessage(Message message) {
        return this.messages.getOrDefault(message, "");
    }

    public void reload(CoreCord plugin) {
        plugin.reloadConfig();

        this.load(plugin);
    }

    public void load(CoreCord plugin) {
        this.cfg = plugin.getConfig();
        this.messages = new EnumMap<>(Message.class);

        this.useMySQL = cfg.getBoolean("use-mysql", true);

        this.host = getStr("mysql-host", "localhost");
        this.database = getStr("mysql-database", "database");
        this.username = getStr("mysql-username", "root");
        this.password = getStr("mysql-password");
        this.port = this.cfg.getInt("mysql-port", 3306);

        this.botToken = getStr("discord-bot-token");

        if (this.botToken.isEmpty() || this.botToken.equals("your-token")) {
            Messenger.warn("Discord bot token is not set in the config! Please set it to use Discord features. The plugin will be disabled.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        this.botPrefix = getStr("command-prefix", "co!");

        this.allowedRoles = cfg.getStringList("allowed-roles");

        this.debugging = cfg.getBoolean("debug");
        this.deleteOnClose = cfg.getBoolean("delete-on-close");
        this.rowsInPage = cfg.getInt("rows-in-page", 5);

        this.showCount = cfg.getBoolean("always-show-count", true);

        this.embedLeft = getStr("embed-page-left");
        this.embedRight = getStr("embed-page-right");
        this.embedClose = getStr("embed-close");
        this.embedColor = getStr("embed-color");
        this.embedTitle = getStr("embed-title");
        this.embedFooter = getStr("embed-footer");

        messages.put(Message.EMBED_SPECIFY_TIME, getStr("messages.discord.specify-time"));
        messages.put(Message.EMBED_CONFIG_RELOAD, getStr("messages.discord.config-reloaded"));
        messages.put(Message.EMBED_RESULT_COUNT, getStr("messages.discord.result-count"));
        messages.put(Message.EMBED_NO_RESULTS_FILTER, getStr("messages.discord.no-results-filter"));
        messages.put(Message.EMBED_NO_RESULTS, getStr("messages.discord.no-results"));
        messages.put(Message.GAME_CONFIG_RELOAD, getStr("messages.game.config-reloaded"));
        messages.put(Message.COORDINATE_ROW, getStr("messages.discord.coordinates"));

        this.dateFormat = getStr("date-format");

        this.cfgVersion = getStr("cfg-version");
        this.notifyUpdate = cfg.getBoolean("notify-update", true);

        this.channelWhitelist = cfg.getBoolean("channel-whitelist", false);
        this.allowedChannels = cfg.getStringList("channels-ids");

        this.excludedData = cfg.getStringList("result-excludes");

        Utils.setDateFormat(this.dateFormat);

        Messenger.debug("Config has been loaded successfully.");
        Messenger.debug("Allowed role IDs to use the commands:");

        for (String r : allowedRoles) {
            Messenger.debug(r);
        }

        loadWebhooks();
    }

    private final List<WebhookLogger> webhooks = new ArrayList<>();

    public List<WebhookLogger> getLoggers() {
        return webhooks;
    }

    public WebhookLogger getWebhook(EActionType action) {
        return webhooks.stream()
                .filter(logger -> logger.hasAction(action))
                .findFirst()
                .orElse(null);
    }

    private void loadWebhooks() {
        this.webhooks.clear();

        ConfigurationSection loggerSection = this.cfg.getConfigurationSection("webhook-loggers");

        if (loggerSection == null) {
            Messenger.warn("No webhook loggers found in config.");
            return;
        }

        for (String key : loggerSection.getKeys(false)) {
            var section = this.cfg.getConfigurationSection("webhook-loggers." + key);

            if (section == null) {
                continue;
            }

            String url = section.getString("url");

            if (url != null && !"webhook-url-goes-here".equals(url)) {
                WebhookLogger webhook = new WebhookLogger(url);

                List<EActionType> actions = section.getStringList("actions").stream()
                        .map(EActionType::fromString)
                        .filter(actionType -> {
                            if (actionType != null) {
                                return true;
                            }

                            Messenger.warn("Webhook logger '" + key + "' has an invalid action type in the config.");

                            return false;
                        })
                        .toList();

                webhook.addActions(actions);

                webhooks.add(webhook);

                Messenger.debug("Loaded webhook logger '" + key + "' with actions: " + actions);
            } else {
                Messenger.warn("Webhook logger '" + key + "' does not have a valid URL.");
            }
        }
    }

    private String getStr(String path) {
        return this.cfg.getString(path, "");
    }

    private String getStr(String path, String def) {
        return this.cfg.getString(path, def);
    }

    public boolean isDebugEnabled() {
        return this.debugging;
    }

    public String getEmbedLeft() {
        if (this.embedLeft.isEmpty())
            return "⬅️";

        return this.embedLeft;
    }

    public String getEmbedRight() {
        if (this.embedLeft.isEmpty()) {
            return "➡️️";
        }

        return this.embedRight;
    }

    public String getEmbedColor() {
        if (this.embedLeft.isEmpty()) {
            return "#00FFFF";
        }

        return this.embedColor;
    }

    public String getEmbedTitle() {
        if (this.embedTitle.isEmpty()) {
            return "CoreCord";
        }

        return this.embedTitle;
    }

    public int getRowsInPage() {
        if (this.rowsInPage > 25) {
            rowsInPage = 25;

            Messenger.debug("Too many rows in one page. Defaulting to 25.");
        }

        return this.rowsInPage;
    }

    public String getCfgVersion() {
        if (cfgVersion == null || cfgVersion.trim().isEmpty())
            return "1.0";

        return cfgVersion;
    }
}