package me.m0dii.CoreCord;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config
{
    public Config()
    {
    
    }
    
    private boolean debugging;
    
    private String host, database, username, password, tablePrefix;
    private int port;
    
    private String botToken, botPrefix;
    
    private List<String> allowedRoles;
    
    FileConfiguration cfg;
    
    public void reload(CoreCord plugin)
    {
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();
        
        this.load(plugin);
    }
    
    public void load(CoreCord plugin)
    {
        this.cfg = plugin.getConfig();
        
        this.host = getStr("mysql-host");
        //this.tablePrefix = getStr("table-prefix");
        this.database = getStr("mysql-database");
        this.username = getStr("mysql-username");
        this.password = getStr("mysql-password");
        this.port = this.cfg.getInt("mysql-port", 0);
        
        this.botToken = getStr("discord-bot-token");
        this.botPrefix = getStr("command-prefix");
        
        this.allowedRoles = cfg.getStringList("allowed-roles");
        
        this.debugging = cfg.getBoolean("debug");
    }
    
    private String getStr(String path)
    {
        return this.cfg.getString(path, "");
    }
    
    public String getHost()
    {
        return this.host;
    }
    
    public String getDatabase()
    {
        return this.database;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    
    public String getPassword()
    {
        return this.password;
    }
    
    public int getPort()
    {
        return this.port;
    }
    
    public String getBotToken()
    {
        return this.botToken;
    }
    
    public String getBotPrefix()
    {
        return this.botPrefix;
    }
    
    public List<String> getAllowedRoles()
    {
        return this.allowedRoles;
    }
    
    public boolean debugEnabled()
    {
        return this.debugging;
    }
}