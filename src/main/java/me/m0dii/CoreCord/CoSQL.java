package me.m0dii.CoreCord;

import me.m0dii.CoreCord.Utils.Utils;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
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
        String sep = File.separator;
        
        boolean debug = cfg.debugEnabled();
        
        if(useMySQL)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
            }
            catch(ClassNotFoundException ex)
            {
                if(debug)
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
                if(debug)
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
                    String dataFolder = CoreProtect.getInstance().getDataFolder().toPath() + sep + "database.db";
                    
                    if(debug)
                        plugin.getLogger().info("JDBC db Location: " + dataFolder);
                    
                    connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                    
                    if(debug)
                        plugin.getLogger().info("Connection null: " + (connection == null));
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
    
            if(debug)
                ex.printStackTrace();
        }
    }
    
    private Table getTableByAction(String action)
    {
        switch(action.toLowerCase().replaceAll("[-+]", ""))
        {
            case "block":
                return Table.BLOCK;
    
            case "command":
                return Table.COMMAND;
    
            case "container":
                return Table.CONTAINER;
                
            case "drop":
                return Table.DROP;
    
            case "chat":
                return Table.CHAT;
                
            case "session":
                return Table.SESSION;
                
            default:
                break;
        }
        
        return null;
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
                userID = result.getInt("ID");
            
            pst.close();
        }
        
        return userID;
    }
    
    private List<String> getIDSbyNames(String[] names) throws SQLException
    {
        if(connection.isClosed())
            connect();
        
        List<String> userIDS = new ArrayList<>();
        
        String query =
        "SELECT co_command.user AS ID, cu.user AS NAME " +
        "FROM co_command " +
        "LEFT JOIN co_user cu ON co_command.user = cu.rowid " +
        "WHERE LOWER(cu.user) IN (";
        
        StringBuilder sb = new StringBuilder();
        
        for(String name : names)
            sb.append("'").append(name).append("',");
        
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") GROUP BY ID; ");
        
        query += sb.toString();
        
        PreparedStatement pst = connection.prepareStatement(query);
        
        try(ResultSet result = pst.executeQuery())
        {
            while (result.next())
                userIDS.add(result.getString("ID"));
        }
    
        pst.close();
        
        if(cfg.debugEnabled())
        {
            plugin.getLogger().info("Joined names: " + String.join(", ", names));
            
            for(String s : userIDS)
                plugin.getLogger().info(s);
        }
        
        return userIDS;
    }
    
    public List<String> lookUpData(String[] names, String action, String[] blocks, long time) throws SQLException
    {
        if(connection == null || connection.isClosed())
            connect();
        
        boolean debug = cfg.debugEnabled();
        
        List<String> results = new ArrayList<>();
        
        int actionType = -1;
        
        if(action.charAt(0) == '-')
            actionType = 0;
        if(action.charAt(0) == '+')
            actionType = 1;
        
        if(debug)
            plugin.getLogger().info(String.valueOf(actionType));
    
        List<String> userIDs = getIDSbyNames(names);
        
        StringBuilder inb = new StringBuilder();
        
        for(String id : userIDs)
            inb.append("'").append(id).append("',");
        
        inb.deleteCharAt(inb.length() - 1);
        
        String in = inb.toString();
        
        Table table = getTableByAction(action);
        
        if(debug)
            plugin.getLogger().info(String.valueOf(time));
        
        if(table == null)
            return results;
    
        if(table.equals(Table.DROP))
        {
            String query =
            "SELECT " +
            "co_item.time AS time, " +
            "co_item.x, " +
            "co_item.y, " +
            "co_item.z, " +
            "cmm.material as material, " +
            "co_item.amount, " +
            "cu.user as player, " +
            "co_item.action as action " +
            "FROM co_item " +
            "LEFT JOIN co_material_map cmm on co_item.type = cmm.id " +
            "LEFT JOIN co_user cu on co_item.user = cu.rowid " +
            "WHERE co_item.time > UNIX_TIMESTAMP() - ? " +
            "AND co_item.user IN ( query_names ) ";
    
            if(action.charAt(0) == '-')
                actionType = 2;
            if(action.charAt(0) == '+')
                actionType = 3;
    
            if(actionType != -1)
                query += " AND co_item.action = " + actionType;
    
            query = query.replace("query_names", in);
            
            getResults(results, query, time);
        }
    
        if(table.equals(Table.CONTAINER))
        {
            String query =
            "SELECT " +
            "co_container.time AS time, " +
            "co_container.x, " +
            "co_container.y, " +
            "co_container.z, " +
            "cmm.material as material, " +
            "co_container.rolled_back, " +
            "co_container.action as action, " +
            "co_container.amount, " +
            "cu.user as player, " +
            "cu.uuid as playeruuid " +
            "FROM co_container " +
            "LEFT JOIN co_material_map cmm on co_container.type = cmm.id " +
            "LEFT JOIN co_user cu on co_container.user = cu.rowid " +
            "WHERE co_container.time > UNIX_TIMESTAMP() - ? " +
            "AND co_container.user IN ( query_names ) ";
    
            if(action.charAt(0) == '-')
                actionType = 0;
            if(action.charAt(0) == '+')
                actionType = 1;
            
            if(actionType != -1)
                query += " AND co_container.action = " + actionType;
            
            query = query.replace("query_names", in);
            
            getResults(results, query, time);
        }
        
        if(table.equals(Table.SESSION))
        {
            String query =
            "SELECT " +
            "co_session.time AS time, " +
            "co_session.x, " +
            "co_session.y, " +
            "co_session.z, " +
            "co_session.action AS action, " +
            "cu.user AS player, " +
            "cu.uuid AS playeruuid " +
            "FROM co_session " +
            "LEFT JOIN co_user cu on co_session.user = cu.rowid " +
            "WHERE co_session.time > UNIX_TIMESTAMP() - ? " +
            "AND co_session.user IN ( query_names ) ";
            
            if(actionType != -1)
                query += " AND co_session.action = " + actionType;
    
            if(!useMySQL)
                query = query.replace("UNIX_TIMESTAMP()",
                        "strftime('%s', 'now')");
            
            query = query.replace("query_names", in);
    
            PreparedStatement pst = connection.prepareStatement(query);
    
            pst.setLong(1, time);

            try(ResultSet result = pst.executeQuery())
            {
                while (result.next())
                {
                    StringBuilder values = new StringBuilder();
            
                    getDateAndXYZ(values, result);
            
                    values.append(result.getString("player"))
                            .append(" ");
            
                    String ac = result.getString("action");
            
                    if(ac.equals("0"))
                        values.append("logged out");
                    if(ac.equals("1"))
                        values.append("logged in");
            
                    results.add(values.toString());
                }
            }
        }
    
        if(table.equals(Table.BLOCK))
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
            "AND co_block.user IN ( query_names ) ";
            
            if(actionType != -1)
                query += " AND co_block.action = " + actionType;
    
            if(!useMySQL)
                query = query.replace("UNIX_TIMESTAMP()",
                        "strftime('%s', 'now')");
    
            query = query.replace("query_names", in);
            
            PreparedStatement pst = connection.prepareStatement(query);
            
            pst.setLong(1, time);
            
            try(ResultSet result = pst.executeQuery())
            {
                while (result.next())
                {
                    StringBuilder values = new StringBuilder();
                    
                    String mat = result.getString("material");
                    
                    if(blocks.length != 0)
                    {
                        boolean skip = true;
                        
                        for(String bl : blocks)
                            if(mat.contains(bl.toLowerCase().trim()))
                            {
                                skip = false;
                                
                                break;
                            }
                        
                        if(skip)
                            continue;
                    }
                    
                    getDateAndXYZ(values, result);
                    
                    values.append(result.getString("player"))
                            .append(" ");
        
                    String ac = result.getString("action");
        
                    if(ac.equals("0"))
                        values.append("destroyed ");
                    
                    if(ac.equals("1"))
                        values.append("placed ");
        
                    values.append(mat);
        
                    results.add(values.toString());
                }
            }
        }
    
        if(table.equals(Table.COMMAND))
        {
            String query =
            "SELECT * FROM co_command " +
            "WHERE co_command.time > UNIX_TIMESTAMP() - ? " +
            "AND co_command.user IN ( query_names ) ";
    
            query = query.replace("query_names", in);
    
            getMessageOrCommand(time, results, query);
        }
    
        if(table.equals(Table.CHAT))
        {
            String query =
            "SELECT " +
            "co_chat.time AS time, " +
            "co_chat.x, " +
            "co_chat.y, " +
            "co_chat.z, " +
            "co_chat.message as message, " +
            "cu.user as player, " +
            "cu.uuid as playeruuid " +
            "FROM co_chat " +
            "LEFT JOIN co_user cu on co_chat.user = cu.rowid " +
            "WHERE co_chat.time > CURRENT_TIMESTAMP - ? " +
            "AND co_chat.user IN ( query_names ) ";
    
            query = query.replace("query_names", in);
        
            getMessageOrCommand(time, results, query);
        }
        
        if(!useMySQL)
            connection.close();
    
        return results;
    }
    
    private void getMessageOrCommand(long time, List<String> results, String query) throws SQLException
    {
        if(!useMySQL)
            query = query.replace("UNIX_TIMESTAMP()", "strftime('%s', 'now')");
        
        PreparedStatement pst = connection.prepareStatement(query);
        
        pst.setLong(1, time);

        try(ResultSet result = pst.executeQuery())
        {
            while (result.next())
            {
                StringBuilder values = new StringBuilder();

                getDateAndXYZ(values, result);

                values.append(result.getString("message"));
    
                results.add(values.toString());
            }
        }
    }
    
    private void getDateAndXYZ(StringBuilder sb, ResultSet result) throws SQLException
    {
        String date = Utils.getDateFromTimestamp(result.getString("time"));
    
        sb.append(date).append(" | ");
    
        sb.append("X:")
                .append(result.getString("x"))
                .append(" ");
    
        sb.append("Y:")
                .append(result.getString("y"))
                .append(" ");
    
        sb.append("Z:")
                .append(result.getString("z"))
                .append("\n");
    }
    
    private void getResults(List<String> results, String query, long time) throws SQLException
    {
        if(!useMySQL)
            query = query.replace("UNIX_TIMESTAMP()",
                    "strftime('%s', 'now')");
        
        PreparedStatement pst = connection.prepareStatement(query);
        
        pst.setLong(1, time);
        
        try(ResultSet result = pst.executeQuery())
        {
            while (result.next())
            {
                StringBuilder values = new StringBuilder();
        
                getDateAndXYZ(values, result);
        
                values.append(result.getString("player"))
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
