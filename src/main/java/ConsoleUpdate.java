import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.SocketException;
import java.util.Properties;

public class ConsoleUpdate implements Runnable{

    private String serverFtpIp;
    private int serverFtpPort;
    private String serverFtpUser;
    private String serverFtpPassword;

    private MessageChannel channel;

    final String MARKED_END = "--- END ---";

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

            InputStream inputStream = ftp.retrieveFileStream(ftpFileArray[0].getName());

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String lastLine = "";
            while (reader.ready()) {
                lastLine = reader.readLine();
            }

            if(!MARKED_END.equals(lastLine)) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                int lineNumber = 1;
                int lastLineNumber = 1;
                while (reader.ready()) {
                    if(MARKED_END.equals(reader.readLine())) {
                        lastLineNumber = lineNumber;
                    }
                    lineNumber++;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                reader.skip(lastLineNumber);
                while (reader.ready()) {
                    //check for streak message
                }
                //update file with MARKED_END and upload it again on the server
                //ftp.storeFile("console.log", inputStream);
            }


           /*
            TODO: send congratulatory message through variable "channel"
            */

        } catch (SocketException ex) {
            System.out.println("Socket Exception");
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }
}
