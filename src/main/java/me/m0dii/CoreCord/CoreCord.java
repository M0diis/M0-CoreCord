package me.m0dii.CoreCord;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.PaginatorBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.*;

public class CoreCord extends JavaPlugin
{
    private static CoreCord instance;
    
    private FileConfiguration fileCfg = null;
    private File configFile = null;
    private Config cfg;
    
    public Config getCfg()
    {
        return this.cfg;
    }
    
    private JDA discord;
    private DiscordListener msgListener;
    
    public JDA getDiscord()
    {
        return discord;
    }
    
    private MySQL coSQL;
    
    public MySQL getCoSQL()
    {
        if(coSQL == null)
            this.coSQL = new MySQL(this);
        
        return this.coSQL;
    }
    
    public void onEnable()
    {
        instance = this;
    
        prepareConfig();
        
        this.cfg = new Config();
        this.cfg.load(this);
    
        this.msgListener = new DiscordListener(this);
        
        this.coSQL = new MySQL(this);
        
        initializeDiscordBOT();
    
        info("");
        info("  __  __  ___  ");
        info(" |  \\/  |/ _ \\ ");
        info(" | \\  / | | | |");
        info(" | |\\/| | | | |");
        info(" | |  | | |_| |");
        info(" |_|  |_|\\___/");
        info("");
        info("M0-CoreCord has been enabled!");
    }
    
    public void onDisable()
    {
        if(discord != null)
            this.discord.shutdownNow();
    }
    
    private void prepareConfig()
    {
        this.configFile = new File(this.getDataFolder(), "config.yml");
        
        if(!this.configFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            this.configFile.getParentFile().mkdirs();
    
            this.copy(this.getResource("config.yml"), this.configFile);
        }
        
        try
        {
            this.getConfig().options().copyDefaults(true);
            this.getConfig().save(this.configFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    
        this.fileCfg = YamlConfiguration.loadConfiguration(this.configFile);
    }
    
    private void initializeDiscordBOT()
    {
        try
        {
            this.discord = JDABuilder.createDefault(cfg.getBotToken())
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
            
            this.discord.awaitReady();
            
            this.discord.addEventListener(msgListener);
    
            Pages.activate(PaginatorBuilder.createSimplePaginator(this.discord));
            
            this.info("Logged in successfully as " + discord.getSelfUser().getAsTag());
        }
        catch(LoginException | InterruptedException | InvalidHandlerException ex)
        {
            this.warning("Discord BOT has failed to connect..");
            this.warning("Please check the configuration and make sure token is correct.");
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
            catch(Exception e)
            {
                this.warning("Error copying resource: " + e.getMessage());
                
                e.printStackTrace();
            }
        }
    }
    
    private void info(String message)
    {
        this.getLogger().info(message);
    }
    
    private void warning(String message)
    {
        this.getLogger().warning(message);
    }
}
