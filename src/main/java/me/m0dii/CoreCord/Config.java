package me.m0dii.CoreCord;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config
{
    public Config()
    {
    
    }
    
    private boolean debugging;
    
    private boolean useMySQL;
    private String host, database, username, password, tablePrefix;
    private int port;
    
    private String embedLeft, embedRight, embedClose;
    private boolean deleteOnClose;
    private int rowsInPage;
    
    private String dateFormat;
    
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
        
        this.embedLeft = getStr("embed-page-left");
        this.embedRight = getStr("embed-page-right");
        this.embedClose = getStr("embed-close");
        
        this.dateFormat = getStr("date-format");
        
        Utils.setDateFormat(this.dateFormat);
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
        return this.rowsInPage;
    }
    
    public boolean useMySQL()
    {
        return this.useMySQL;
    }
}