import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Downloader
{
    private static final String sqliteJDBC =
            "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.34.0/sqlite-jdbc-3.34.0.jar";
    
    private static final String mysqlJDBC =
            "https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.24.tar.gz";
    
    private static final String libsDirectory = "." + File.separator + "plugins" + File.separator + "libs" + File.separator;
    
    public static void downloadJDBC(String url, String filename) throws IOException
    {
        File file = new File(libsDirectory + File.separator + filename);
        
        if(!file.exists())
            file.getParentFile().mkdirs();
        
        URL download = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(download.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        rbc.close();
        fos.close();
    }
    
    private static void unTarFile(File tarFile, File destFile) throws IOException
    {
        FileInputStream fis = new FileInputStream(tarFile);
        TarArchiveInputStream tis = new TarArchiveInputStream(fis);
        TarArchiveEntry tarEntry = null;
        
        while ((tarEntry = tis.getNextTarEntry()) != null)
        {
            File outputFile = new File(destFile + File.separator + tarEntry.getName());
            
            if(tarEntry.isDirectory())
            {
                if(!outputFile.exists())
                    outputFile.mkdirs();
            }
            else
            {
                outputFile.getParentFile().mkdirs();
                
                FileOutputStream fos = new FileOutputStream(outputFile);
                IOUtils.copy(tis, fos);
                
                fos.close();
            }
        }
        
        tis.close();
    }
    
    
    public static void main(String[] args)
    {
        try
        {
            downloadJDBC(sqliteJDBC, "sqlite-jdbc-3.34.0.jar");
            downloadJDBC(mysqlJDBC, "mysql-connector-java-8.0.24.tar.gz");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    
        
        
        File tarFile = new File(libsDirectory + File.separator + "mysql-connector-java-8.0.24.tar.gz");
        File jarFile = new File(libsDirectory);
    
        try
        {
            unTarFile(tarFile, jarFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
