package me.m0dii.corecord;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.PaginatorBuilder;
import com.github.ygimenez.type.Action;
import lombok.Getter;
import me.m0dii.corecord.commands.CoreCoCommand;
import me.m0dii.corecord.listeners.DiscordListener;
import me.m0dii.corecord.listeners.LoggerListener;
import me.m0dii.corecord.listeners.PlayerJoin;
import me.m0dii.corecord.utils.Config;
import me.m0dii.corecord.utils.Messenger;
import me.m0dii.corecord.utils.UpdateChecker;
import me.m0dii.corecord.utils.WebhookLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CoreCord extends JavaPlugin {
    @Getter
    private static CoreCord instance;

    @Getter
    private Config cfg;

    public String getSpigotLink() {
        return "https://www.spigotmc.org/resources/91863/";
    }

    private JDA discord;
    private DiscordListener msgListener;

    public JDA getDiscord() {
        if (this.discord == null) {
            initializeDiscordBOT();
        }

        return this.discord;
    }

    private CoSQL coSQL;

    public CoSQL getCoSQL() {
        if (coSQL == null) {
            this.coSQL = new CoSQL(this);
        }

        return this.coSQL;
    }

    @Override
    public void onEnable() {
        instance = this;

        prepareConfig();

        PluginManager pm = getServer().getPluginManager();

        this.msgListener = new DiscordListener(this);

        this.coSQL = new CoSQL(this);

        pm.registerEvents(new PlayerJoin(this), this);
        pm.registerEvents(new LoggerListener(this), this);

        Optional.ofNullable(getCommand("corecord"))
                .ifPresent(cmd -> cmd.setExecutor(new CoreCoCommand(this)));

        setupMetrics();

        initializeDiscordBOT();

        Messenger.info("");
        Messenger.info("  __  __  ___  ");
        Messenger.info(" |  \\/  |/ _ \\ ");
        Messenger.info(" | \\  / | | | |");
        Messenger.info(" | |\\/| | | | |");
        Messenger.info(" | |  | | |_| |");
        Messenger.info(" |_|  |_|\\___/");
        Messenger.info("");
        Messenger.info("M0-CoreCord has been enabled!");

        checkForUpdates();
    }

    private void checkForUpdates() {
        new UpdateChecker(this, 91863).getVersion(ver ->
        {
            String curr = this.getPluginMeta().getVersion();

            if (!curr.equalsIgnoreCase(
                    ver.replace("v", ""))) {
                Messenger.info("You are running an outdated version of M0-CoreCord.");
                Messenger.info("Latest version: " + ver + ", you are using: " + curr);
                Messenger.info("You can download the latest version on Spigot:");
                Messenger.info(getSpigotLink());
            }
        });
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this, 11173);

        CustomChart c = new MultiLineChart("players_and_servers", () ->
        {
            Map<String, Integer> valueMap = new HashMap<>();

            valueMap.put("servers", 1);
            valueMap.put("players", Bukkit.getOnlinePlayers().size());

            return valueMap;
        });

        metrics.addCustomChart(c);
    }

    @Override
    public void onDisable() {
        if (discord != null) {
            this.discord.shutdownNow();
        }

        if (CoSQL.connection != null) {
            try {
                if (!CoSQL.connection.isClosed()) {
                    CoSQL.connection.close();

                    Messenger.info("SQL connection has been closed successfully.");
                }

            } catch (SQLException ex) {
                Messenger.debug(ex.getMessage());

                Messenger.warn("Failed to close SQL connection..");
            }
        }

        for (WebhookLogger logger : cfg.getLoggers().values()) {
            logger.close();
        }

        Messenger.info("M0-CoreCord has been disabled.");
    }

    private void prepareConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();

            this.copy(this.getResource("config.yml"), configFile);
        }

        YamlConfiguration.loadConfiguration(configFile);

        this.cfg = new Config();
        this.cfg.load(this);

        this.copy(this.getResource("config.yml_default"), new File(this.getDataFolder(), "config.yml_default"));
    }

    private void initializeDiscordBOT() {
        try {
            this.discord = JDABuilder.createDefault(cfg.getBotToken())
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS
                    )
                    .build();

            this.discord.awaitReady();

            this.discord.addEventListener(msgListener);

            PaginatorBuilder paginator = PaginatorBuilder.createPaginator()
                    .setHandler(this.discord)
                    .shouldRemoveOnReact(true)
                    .setEmote(Action.CANCEL, this.cfg.getEmbedClose())
                    .setEmote(Action.NEXT, this.cfg.getEmbedRight())
                    .setEmote(Action.PREVIOUS, this.cfg.getEmbedLeft())
                    .setDeleteOnCancel(this.cfg.isDeleteOnClose());

            Pages.activate(paginator.build());

            Messenger.info("Logged in successfully as " + discord.getSelfUser().getAsTag());
        } catch (InterruptedException | InvalidHandlerException ex) {
            Messenger.warn("Discord BOT has failed to connect..");
            Messenger.warn("Please check the configuration and make sure token is correct.");

            Messenger.debug(ex.getMessage());
        }
    }

    private void copy(InputStream in, File file) {
        if (in != null) {
            try {
                OutputStream out = new FileOutputStream(file);

                byte[] buf = new byte[1024];

                int len;

                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

                out.close();
                in.close();
            } catch (Exception ex) {
                Messenger.warn("Error copying config file..");

                Messenger.debug(ex.getMessage());
            }
        }
    }
}
