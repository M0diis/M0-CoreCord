package me.m0dii.corecord.utils;

import me.m0dii.corecord.CoreCord;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config
{
    private String cfgVersion;
    private boolean debugging;
    
    private boolean useMySQL;
    private String host, database, username, password; // tablePrefix;
    private int port;
    
    private String embedLeft, embedRight, embedClose;
    private boolean deleteOnClose;
    private int rowsInPage;
    
    private boolean showCount;
    
    private String dateFormat;
    
    private String botToken, botPrefix;
    
    private boolean channelWhitelist;
    
    private List<String> allowedRoles, allowedChannels;
    
    private boolean notifyUpdate;
    
    FileConfiguration cfg;
    
    public void reload(CoreCord plugin)
    {
        plugin.reloadConfig();
        
        this.load(plugin);
    }
    
    public void load(CoreCord plugin)
    {
        this.cfg = plugin.getConfig();
        
        this.useMySQL = cfg.getBoolean("use-mysql", true);
        
        this.host = getStr("mysql-host", "localhost");
        //this.tablePrefix = getStr("table-prefix");
        this.database = getStr("mysql-database", "database");
        this.username = getStr("mysql-username", "root");
        this.password = getStr("mysql-password");
        this.port = this.cfg.getInt("mysql-port", 3306);
        
        this.botToken = getStr("discord-bot-token");
        this.botPrefix = getStr("command-prefix", "co!");
        
        this.allowedRoles = cfg.getStringList("allowed-roles");
        
        this.debugging = cfg.getBoolean("debug");
        this.deleteOnClose = cfg.getBoolean("delete-on-close");
        this.rowsInPage = cfg.getInt("rows-in-page", 5);
        
        this.showCount = cfg.getBoolean("always-show-count", true);
        
        this.embedLeft = getStr("embed-page-left");
        this.embedRight = getStr("embed-page-right");
        this.embedClose = getStr("embed-close");
        
        this.dateFormat = getStr("date-format");
        
        this.cfgVersion = getStr("cfg-version");
        this.notifyUpdate = cfg.getBoolean("notify-update", true);
        
        this.channelWhitelist = cfg.getBoolean("channel-whitelist", false);
        this.allowedChannels = cfg.getStringList("channels-ids");
        
        Utils.setDateFormat(this.dateFormat);
        
        Messenger.debug("Config has been loaded successfully.");
        Messenger.debug("Allowed role IDs to use the commands:");
        
        for(String r : allowedRoles)
            Messenger.debug(r);
    }
    
    private String getStr(String path)
    {
        return this.cfg.getString(path, "");
    }
    
    private String getStr(String path, String def)
    {
        return this.cfg.getString(path, def);
    }
    
    public String getHost()
    {
        Messenger.debug("Host has been set to " + this.host);
        
        return this.host;
    }
    
    public String getDatabase()
    {
        Messenger.debug("Database has been set to " + this.database);
        
        return this.database;
    }
    
    public String getUsername()
    {
        Messenger.debug("Username has been set to " + this.username);
        
        return this.username;
    }
    
    public String getPassword()
    {
        return this.password;
    }
    
    public int getPort()
    {
        Messenger.debug("Port has been set to " + this.database);
        
        return this.port;
    }
    
    public String getBotToken()
    {
        return this.botToken;
    }
    
    public String getBotPrefix()
    {
        Messenger.debug("Bot prefix has been set to " + this.botPrefix);
        
        return this.botPrefix;
    }
    
    public List<String> getAllowedRoles()
    {
        return this.allowedRoles;
    }
    
    public boolean debugEnabled()
    {
        Messenger.debug("Debug has been enabled.");
        
        return this.debugging;
    }
    
    public String getEmbedLeft()
    {
        if(this.embedLeft.isEmpty())
            return "⬅️";
        
        return this.embedLeft;
    }
    
    public String getEmbedRight()
    {
        if(this.embedLeft.isEmpty())
            return "➡️️";
        
        return this.embedRight;
    }
    
    public boolean deleteOnClose()
    {
        return this.deleteOnClose;
    }
    
    public String getEmbedClose()
    {
        return this.embedClose;
    }
    
    public int getRowsInPage()
    {
        if(this.rowsInPage > 25)
        {
            rowsInPage = 25;
            
            Messenger.debug("Too many rows in one page. Setting to 25.");
        }
        
        return this.rowsInPage;
    }
    
    public String getDateFormat()
    {
        return this.dateFormat;
    }
    
    public boolean useMySQL()
    {
        return this.useMySQL;
    }
    
    public boolean showCount()
    {
        return this.showCount;
    }
    
    public boolean notifyUpdate()
    {
        return this.notifyUpdate;
    }
    
    public String getCfgVersion()
    {
        if(cfgVersion == null || cfgVersion.trim().isEmpty())
            return "1.0";
        
        return cfgVersion;
    }
    
    public List<String> getAllowedChannels()
    {
        return this.allowedChannels;
    }
    
    public boolean channelWhitelist()
    {
        return this.channelWhitelist;
    }
}