package me.m0dii.CoreCord;

public class CommandResults
{
    private long timeStamp;
    
    private int xCord;
    private int yCord;
    private int zCord;
    
    private String message;
    
    public CommandResults(long timeStamp, int x, int y, int z, String message)
    {
        this.timeStamp = timeStamp;
        
        this.xCord = x;
        this.yCord = y;
        this.zCord = z;
        
        this.message = message;
    }
    
    public int getyCord()
    {
        return yCord;
    }
    
    public int getxCord()
    {
        return xCord;
    }
    
    public int getzCord()
    {
        return zCord;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public long getTimeStamp()
    {
        return timeStamp;
    }
}
