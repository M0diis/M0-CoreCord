package me.m0dii.CoreCord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoSQL
{
    static Connection connection;
    
    private boolean useMySQL;
    private String host, database, username, password;
    private int port;
    
    private final Config cfg;
    
    private final CoreCord plugin;
    
    public CoSQL(CoreCord plugin)
    {
        this.cfg = plugin.getCfg();
        this.plugin = plugin;
    
        this.setUpConnection();
    }
    
    public void setUpConnection()
    {
        this.useMySQL = cfg.useMySQL();
        
        this.host = cfg.getHost();
        this.database = cfg.getDatabase();
        this.username = cfg.getUsername();
        this.password = cfg.getPassword();
        this.port = cfg.getPort();
        
        this.connect();
    }
    
    public void connect()
    {
        if(useMySQL)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
            }
            catch(ClassNotFoundException ex)
            {
                if(this.cfg.debugEnabled())
                    ex.printStackTrace();
                else
                    plugin.getLogger().info("Cannot find MySQL driver..");
            }
        }
        else
        {
            try
            {
                Class.forName("org.sqlite.JDBC");
            }
            catch(ClassNotFoundException ex)
            {
                if(this.cfg.debugEnabled())
                    ex.printStackTrace();
                else
                    plugin.getLogger().info("Cannot find SQLite driver..");
            }
        }
        
        try
        {
            if(useMySQL)
            {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database,
                        this.username, this.password);
            }
            else
            {
                Plugin cp = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
    
                if(cp != null)
                {
                    String dataFolder = cp.getDataFolder().toPath() + "\\" + "database.db";
                    
                    connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                }
                else
                {
                    plugin.getLogger().warning("Failed to connect to SQLite database..");
                }

            }
        }
        catch(SQLException ex)
        {
            plugin.getLogger().warning("Failed to connect to the database.");
            plugin.getLogger().warning("Please check the config.");
    
            if(this.cfg.debugEnabled())
                ex.printStackTrace();
        }
    }
    
    private String getTableByAction(String action)
    {
        String table = "";
        
        switch(action.toLowerCase().replaceAll("[-+]", ""))
        {
            case "block":
                table = "co_block";
                break;
    
            case "command":
                table = "co_command";
                break;
    
            case "container":
                table = "co_container";
                break;
                
            case "drop":
                table = "co_drop";
                break;
                
            default:
                break;
        }
        
        return table;
    }
    
    private int getIDbyName(String name) throws SQLException
    {
        if(connection.isClosed())
            connect();
        
        String query =
        "SELECT * FROM" +
        "(SELECT co_command.user AS ID, cu.user AS NAME " +
        "FROM co_command " +
        "LEFT JOIN co_user cu ON co_command.user = cu.rowid " +
        "AND cu.user = ? " +
        "GROUP BY ID) AS `NAMES_IDS` " +
        "WHERE NAME = ? ";
    
        PreparedStatement pst = connection.prepareStatement(query);
        
        pst.setString(1, name);
        pst.setString(2, name);
    
        int userID = 0;
    
    
        try(ResultSet result = pst.executeQuery())
        {
            while (result.next())
            {
                userID = result.getInt("ID");
            }
    
            pst.close();
        }
        

        return userID;
    }
    
    private List<Integer> getIDSbyNames(String[] names) throws SQLException
    {
        if(connection.isClosed())
            connect();
        
        List<Integer> userIDS = new ArrayList<>();
        
        String query =
        "SELECT co_command.user AS ID, cu.user as NAME " +
        "FROM co_command " +
        "LEFT JOIN co_user cu on co_command.user = cu.rowid ";
        
        for(String name : names)
            query += "AND cu.user = '" + name + "' ";
    
        query += " GROUP BY ID;";
        
        PreparedStatement pst = connection.prepareStatement(query);
        
        try(ResultSet result = pst.executeQuery())
        {
            if (result.next())
                userIDS.add(result.getInt("ID"));
        }
    
        pst.close();
        
        return userIDS;
    }
    
    public List<String> lookUpData(String name, String action, long time) throws SQLException
    {
        if(connection == null || connection.isClosed())
            connect();
        
        List<String> results = new ArrayList<>();
        
        int actionType = -1;
        
        if(action.charAt(0) == '-')
            actionType = 0;
        if(action.charAt(0) == '+')
            actionType = 1;
    
        int userID = getIDbyName(name);
        
        if(cfg.debugEnabled())
            plugin.getLogger().info("Found user ID by name from database: " + userID);
    
        String table = getTableByAction(action);
        
        if(table.contains("drop"))
        {
            String query =
            "SELECT " +
            "co_item.time AS time, " +
            "co_item.x, " +
            "co_item.y, " +
            "co_item.z, " +
            "cmm.material, " +
            "co_item.amount, " +
            "cu.user as player, " +
            "co_item.action as action " +
            "FROM co_item " +
            "LEFT JOIN co_material_map cmm on co_item.type = cmm.id " +
            "LEFT JOIN co_user cu on co_item.user = cu.rowid " +
            "WHERE co_item.time > UNIX_TIMESTAMP() - ? " +
            "AND co_item.user = ? ";
    
            getResults(results, query, time, userID);
        }
        
        if(table.contains("container"))
        {
            String query =
            "SELECT " +
            "co_container.time AS time, " +
            "co_container.x, " +
            "co_container.y, " +
            "co_container.z, " +
            "cmm.material, " +
            "co_container.rolled_back, " +
            "co_container.action as action, " +
            "co_container.amount, " +
            "cu.user as player, " +
            "cu.uuid as playeruuid " +
            "FROM co_container " +
            "LEFT JOIN co_material_map cmm on co_container.type = cmm.id " +
            "LEFT JOIN co_user cu on co_container.user = cu.rowid " +
            "WHERE co_container.time > UNIX_TIMESTAMP() - ? " +
            "AND co_container.user = ? ";
            
            getResults(results, query, time, userID);
        }
        
        if(table.contains("block"))
        {
            String query =
            "SELECT " +
            "co_block.time AS time, " +
            "co_block.x, " +
            "co_block.y, " +
            "co_block.z, " +
            "cmm.material, " +
            "co_block.rolled_back, " +
            "co_block.action as action, " +
            "cu.user as player, " +
            "cu.uuid as playeruuid " +
            "FROM co_block " +
            "LEFT JOIN co_material_map cmm on co_block.type = cmm.id " +
            "LEFT JOIN co_user cu on co_block.user = cu.rowid " +
            "WHERE co_block.time > UNIX_TIMESTAMP() - ? " +
            "AND co_block.user = ? ";
            
            if(actionType != -1)
                query += " AND co_block.action = " + actionType;
    
            if(!useMySQL)
                query = query.replace("UNIX_TIMESTAMP()", "strftime('%s', 'now')");
            
            PreparedStatement pst = connection.prepareStatement(query);
            
            pst.setLong(1, time);
            pst.setInt(2, userID);
            
            try(ResultSet result = pst.executeQuery())
            {
                while (result.next())
                {
                    StringBuilder values = new StringBuilder();
        
                    String date = Utils.getDateFromTimestamp(result.getString("time"));
        
                    values.append(date)
                            .append(" | ");
        
                    values.append("X:")
                            .append(result.getString("x"))
                            .append(" ");
        
                    values.append("Y:")
                            .append(result.getString("y"))
                            .append(" ");
        
                    values.append("Z:")
                            .append(result.getString("z"))
                            .append(" ");
        
                    values.append("\n")
                            .append(result.getString("player"))
                            .append(" ");
        
                    String ac = result.getString("action");
        
                    if(ac.equals("0"))
                        values.append("destroyed ");
                    if(ac.equals("1"))
                        values.append("placed ");
        
                    values.append(result.getString("material"));
        
                    results.add(values.toString());
                }
            }
        }
        
        if(table.contains("command"))
        {
            String query =
            "SELECT * FROM co_command " +
            "WHERE co_command.time > UNIX_TIMESTAMP() - ? " +
            "AND co_command.user = ?;";
    
            if(!useMySQL)
                query = query.replace("UNIX_TIMESTAMP()", "strftime('%s', 'now')");
            
            PreparedStatement pst = connection.prepareStatement(query);
            
            pst.setLong(1, time);
            pst.setInt(2, userID);
            
            try(ResultSet result = pst.executeQuery())
            {
                while (result.next())
                {
                    StringBuilder values = new StringBuilder();
        
                    String date = Utils.getDateFromTimestamp(result.getString("time"));
        
                    values.append(date)
                            .append(" | ");
        
                    values.append("X:")
                            .append(result.getString("x"))
                            .append(" ");
        
                    values.append("Y:")
                            .append(result.getString("y"))
                            .append(" ");
        
                    values.append("Z:")
                            .append(result.getString("z"))
                            .append(" ");
        
                    values.append("\n")
                            .append(result.getString("message"));
        
                    results.add(values.toString());
                }
            }
        }
        
        if(!useMySQL)
            connection.close();
    
        return results;
    }
    
    private void getResults(List<String> results, String query,
                            long time, int userID) throws SQLException
    {
        if(!useMySQL)
            query = query.replace("UNIX_TIMESTAMP()", "strftime('%s', 'now')");
        
        PreparedStatement pst = connection.prepareStatement(query);
        
        pst.setLong(1, time);
        pst.setInt(2, userID);
        
        if(cfg.debugEnabled())
            plugin.getLogger().info("Executing query: \n" + query);
        
        try(ResultSet result = pst.executeQuery())
        {
            while (result.next())
            {
                StringBuilder values = new StringBuilder();
        
                String date = Utils.getDateFromTimestamp(result.getString("time"));
        
                values.append(date)
                        .append(" | ");
        
                values.append("X:")
                        .append(result.getString("x"))
                        .append(" ");
        
                values.append("Y:")
                        .append(result.getString("y"))
                        .append(" ");
        
                values.append("Z:")
                        .append(result.getString("z"))
                        .append(" ");
        
                values.append("\n")
                        .append(result.getString("player"))
                        .append(" ");
        
                String ac = result.getString("action");
        
                if(ac.equals("0"))
                    values.append("removed ");
                if(ac.equals("1"))
                    values.append("added ");
                if(ac.equals("2"))
                    values.append("dropped ");
                if(ac.equals("3"))
                    values.append("picked up ");
        
                values.append(result.getString("amount"))
                        .append(" ");
        
                values.append(result.getString("material"));
        
                results.add(values.toString());
            }
        }
    }
}
