package me.m0dii.CoreCord;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoSQL
{
    static Connection connection;
    
    private String host, database, username, password;
    private int port;
    
    private final Config cfg;
    
    private final CoreCord plugin;
    
    public CoSQL(CoreCord plugin)
    {
        this.cfg = plugin.getCfg();
        this.plugin = plugin;
    
        this.setUpMySQL();
    }
    
    public void setUpMySQL()
    {
        this.host = cfg.getHost();
        this.database = cfg.getDatabase();
        this.username = cfg.getUsername();
        this.password = cfg.getPassword();
        this.port = cfg.getPort();
        
        this.connect();
    }
    
    public void connect()
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
                plugin.getLogger().info("Cannot find jdbc driver..");
        }
        
        try
        {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database,
                    this.username, this.password);
        }
        catch(SQLException ex)
        {
            plugin.getLogger().warning("Failed to connect to MySQL database.");
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
        
        String query = "SELECT co_command.user AS ID, cu.user as NAME " +
        "FROM co_command " +
        "LEFT JOIN co_user cu on co_command.user = cu.rowid " +
        "AND cu.user = '" + name + "' " +
        "GROUP BY ID ";
    
        Statement st = connection.createStatement();
    
        ResultSet result = st.executeQuery(query);
        
        int userID = 0;
    
        if (result.next())
            userID = result.getInt("ID");
        
        return userID;
    }
    
    public List<String> lookUpData(String name, String action, long time) throws SQLException
    {
        if(connection.isClosed())
            connect();
        
        List<String> results = new ArrayList<>();
        
        int actionType = -1;
        
        if(action.charAt(0) == '-')
            actionType = 0;
    
        if(action.charAt(0) == '+')
            actionType = 1;
    
        int userID = getIDbyName(name);
    
        String table = getTableByAction(action);
    
        Statement st = connection.createStatement();
    
        if(table.contains("drop"))
        {
            String query = "SELECT " +
            "co_item.time AS time, " +
            "co_item.x, " +
            "co_item.y, " +
            "co_item.z, " +
            "cmm.material, " +
            "co_item.amount, " +
            "cu.user as player, " +
            "IF(co_item.action = 2, 'dropped', 'picked up') as action " +
            "FROM co_item " +
            "LEFT JOIN co_material_map cmm on co_item.type = cmm.id " +
            "LEFT JOIN co_user cu on co_item.user = cu.rowid " +
            "WHERE from_unixtime(co_item.time) > CURRENT_TIMESTAMP - " + time + " " +
            "AND co_item.user = " + userID + " ";
    
            getResults(results, st, query);
        }
        
        if(table.contains("container"))
        {
            String query = "SELECT " +
                "co_container.time AS time, " +
                "co_container.x, " +
                "co_container.y, " +
                "co_container.z, " +
                "cmm.material, " +
                "co_container.rolled_back, " +
                "IF(co_container.action = 0, 'removed', 'added') as action, " +
                "co_container.amount, " +
                "cu.user as player, " +
                "cu.uuid as playeruuid " +
                "FROM co_container " +
                "LEFT JOIN co_material_map cmm on co_container.type = cmm.id " +
                "LEFT JOIN co_user cu on co_container.user = cu.rowid " +
                "WHERE from_unixtime(co_container.time) > CURRENT_TIMESTAMP - " + time + " " +
                "AND co_container.user = " + userID + " ";
    
            getResults(results, st, query);
        }
        
        if(table.contains("block"))
        {
            String query = "SELECT " +
                    "co_block.time AS time, " +
                    "co_block.x, " +
                    "co_block.y, " +
                    "co_block.z, " +
                    "cmm.material, " +
                    "co_block.rolled_back, " +
                    "IF(co_block.action = 0, 'destroyed', 'placed') as action, " +
                    "cu.user as player, " +
                    "cu.uuid as playeruuid " +
                    "FROM co_block " +
                    "LEFT JOIN co_material_map cmm on co_block.type = cmm.id " +
                    "LEFT JOIN co_user cu on co_block.user = cu.rowid " +
                    "WHERE from_unixtime(co_block.time) > CURRENT_TIMESTAMP - " + time + " " +
                    "AND co_block.user = " + userID + " ";
            
            if(actionType != -1)
                query += " AND co_block.action = " + actionType;
            
            ResultSet result = st.executeQuery(query);
    
            while (result.next())
            {
                StringBuilder values = new StringBuilder();
        
                String date = getDateFromTimestamp(result.getString("time"));
        
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
    
                values.append(result.getString("action"))
                        .append(" ");
        
                values.append(result.getString("material"));
        
                results.add(values.toString());
            }
        }
    
        if(table.contains("command"))
        {
            String query = "SELECT * FROM co_command " +
            "WHERE from_unixtime(co_command.time) > CURRENT_TIMESTAMP - " + time + " " +
            "AND co_command.user = " + userID + ";";
    
            ResultSet result = st.executeQuery(query);
    
            while (result.next())
            {
                StringBuilder values = new StringBuilder();
    
                String date = getDateFromTimestamp(result.getString("time"));
                
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
        
        return results;
    }
    
    private void getResults(List<String> results, Statement st, String query) throws SQLException
    {
        ResultSet result = st.executeQuery(query);
        
        while (result.next())
        {
            StringBuilder values = new StringBuilder();
        
            String date = getDateFromTimestamp(result.getString("time"));
        
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
        
            values.append(result.getString("action"))
                    .append(" ");
        
            values.append(result.getString("amount"))
                    .append(" ");
        
            values.append(result.getString("material"));
        
            results.add(values.toString());
        }
    }
    
    private String getDateFromTimestamp(String timestamp)
    {
        Instant instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
    
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
        Date date = Date.from(instant);
        
        return format.format(date);
    }
    
    public String getExecutedCommands(String name, int days) throws SQLException
    {
        if(connection.isClosed())
            connect();
        
        int userID = getIDbyName(name);
        
        long time = 86400L * days;
        
        String query = "SELECT COUNT(*) AS AMOUNT FROM co_command" +
                " WHERE from_unixtime(co_command.time) > CURRENT_TIMESTAMP - " + time +
                " AND co_command.user = " + userID + ";";
        
        Statement st1 = connection.createStatement();
        
        ResultSet result1 = st1.executeQuery(query);
        
        if (result1.next())
        {
            return result1.getString("AMOUNT");
        }
        
        return "0";
    }
}
