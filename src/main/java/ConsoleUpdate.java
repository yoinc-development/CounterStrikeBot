import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;

public class ConsoleUpdate implements Runnable{

    private String serverFtpIp;
    private int serverFtpPort;
    private String serverFtpUser;
    private String serverFtpPassword;

    private MessageChannel channel;

    public ConsoleUpdate(Properties properties, MessageChannel channel) {
        serverFtpIp = properties.getProperty("server.ftp.ip");
        serverFtpPort = Integer.parseInt(properties.getProperty("server.ftp.port"));
        serverFtpUser = properties.getProperty("server.ftp.user");
        serverFtpPassword = properties.getProperty("server.ftp.password");

        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            FTPClient ftp = new FTPClient();
            ftp.connect(serverFtpIp, serverFtpPort);
            ftp.login(serverFtpUser, serverFtpPassword);
            ftp.changeWorkingDirectory("/csgo/csgo");

            FTPFile[] ftpFileArray = ftp.listFiles("console.log");

            /*
            TODO: implement reading process of ftpFileArray
            - ftpFileArray.length should only be one since in the current directoy
            only one file named "console.log" should exist.
            this file has to be read for a certain amount of time to await the key
            message "The terrorists have won 10 rounds in a row!"
            TODO: send congratulatory message through variable "channel"
             */

        } catch (SocketException ex) {
            System.out.println("Socket Exception");
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }
}
