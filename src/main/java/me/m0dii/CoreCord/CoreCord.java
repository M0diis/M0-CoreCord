package me.m0dii.CoreCord;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.PaginatorBuilder;
import com.github.ygimenez.type.Emote;
import me.m0dii.CoreCord.Commands.CoreCo;
import me.m0dii.CoreCord.Listeners.DiscordListener;
import me.m0dii.CoreCord.Listeners.PlayerJoin;
import me.m0dii.CoreCord.Utils.Config;
import me.m0dii.CoreCord.Utils.Messenger;
import me.m0dii.CoreCord.Utils.UpdateChecker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CoreCord extends JavaPlugin
{
    public static CoreCord instance;
    
    private Config cfg;
    
    private PluginManager pm;
    
    public String getSpigotLink()
    {
        return "https://www.spigotmc.org/resources/91863/";
    }
    
    public Config getCfg()
    {
        return this.cfg;
    }
    
    private JDA discord;
    private DiscordListener msgListener;
    
    public JDA getDiscord()
    {
        if(this.discord == null)
            initializeDiscordBOT();
        
        return this.discord;
    }
    
    private CoSQL coSQL;
    
    public CoSQL getCoSQL()
    {
        if(coSQL == null)
            this.coSQL = new CoSQL(this);
        
        return this.coSQL;
    }
    
    public void onEnable()
    {
        instance = this;
        
        prepareConfig();
        
        this.pm = getServer().getPluginManager();
        
        this.cfg = new Config();
        this.cfg.load(this);
    
        this.msgListener = new DiscordListener(this);
        
        this.coSQL = new CoSQL(this);
    
        pm.registerEvents(new PlayerJoin(this), this);
    
        PluginCommand cmd = this.getCommand("corecord");
    
        if(cmd != null)
            cmd.setExecutor(new CoreCo(this));
        
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
    
    private void checkForUpdates()
    {
        new UpdateChecker(this, 91863).getVersion(ver ->
        {
            String curr = this.getDescription().getVersion();
            
            if (!curr.equalsIgnoreCase(
                    ver.replace("v", "")))
            {
                Messenger.info("You are running an outdated version of M0-CoreCord.");
                Messenger.info("Latest version: " + ver + ", you are using: " + curr);
                Messenger.info("You can download the latest version on Spigot:");
                Messenger.info(getSpigotLink());
            }
        });
    }
    
    private void setupMetrics()
    {
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
    
    public void onDisable()
    {
        if(discord != null)
            this.discord.shutdownNow();
        
        if(CoSQL.connection != null)
        {
            try
            {
                if(!CoSQL.connection.isClosed())
                {
                    CoSQL.connection.close();
    
                    Messenger.info("SQL connection has been closed successfully.");
                }

            }
            catch(SQLException ex)
            {
                Messenger.debug(ex.getMessage());
                
                Messenger.warn("Failed to close SQL connection..");
            }
        }
    
        Messenger.info("M0-CoreCord has been disabled.");
    }
    
    private void prepareConfig()
    {
        File configFile = new File(this.getDataFolder(), "config.yml");
        
        if(!configFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
    
            this.copy(this.getResource("config.yml"), configFile);
        }
    
        YamlConfiguration.loadConfiguration(configFile);
    }
    
    private void initializeDiscordBOT()
    {
        try
        {
            this.discord = JDABuilder.createDefault(cfg.getBotToken())
                    .build();
            
            this.discord.awaitReady();
            
            this.discord.addEventListener(msgListener);
    
            PaginatorBuilder paginator = PaginatorBuilder.createPaginator()
                    .setHandler(this.discord)
                    .shouldRemoveOnReact(true)
                    .setEmote(Emote.CANCEL, this.cfg.getEmbedClose())
                    .setEmote(Emote.NEXT, this.cfg.getEmbedRight())
                    .setEmote(Emote.PREVIOUS, this.cfg.getEmbedLeft())
                    .setDeleteOnCancel(this.cfg.deleteOnClose());
            
            Pages.activate(paginator.build());
    
            Messenger.info("Logged in successfully as " + discord.getSelfUser().getAsTag());
        }
        catch(LoginException | InterruptedException | InvalidHandlerException ex)
        {
            Messenger.warn("Discord BOT has failed to connect..");
            Messenger.warn("Please check the configuration and make sure token is correct.");
    
            Messenger.debug(ex.getMessage());
        }
    }
    
    private void copy(InputStream in, File file)
    {
        if(in != null)
        {
            try
            {
                OutputStream out = new FileOutputStream(file);
                
                byte[] buf = new byte[1024];
                
                int len;
                
                while((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
                
                out.close();
                in.close();
            }
            catch(Exception ex)
            {
                Messenger.warn("Error copying config file..");
                
                Messenger.debug(ex.getMessage());
            }
        }
    }
}
