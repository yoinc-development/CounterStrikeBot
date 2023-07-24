import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.SocketException;
import java.util.Properties;

public class ConsoleUpdate {

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

    public void congratulateStreakWinners() {
        try {
            BufferedReader reader = retrieveBufferedReader();

            String lastLine = "";
            while (reader.ready()) {
                lastLine = reader.readLine();
            }

            if(lastLine.startsWith(MARKED_END)) {
                reader = retrieveBufferedReader();
                int lineNumber = 1;
                int lastLineNumber = 1;
                while (reader.ready()) {
                    if(reader.readLine().startsWith(MARKED_END)) {
                        lastLineNumber = lineNumber;
                    }
                    lineNumber++;
                }

                reader = retrieveBufferedReader();
                reader.skip(lastLineNumber);
                while (reader.ready()) {
                    if(reader.readLine().startsWith("A full streak has been reached")) {
                        System.out.printf("winner found");
                        channel.sendMessage("winner found").queue();
                    }
                }

                String newContent = "";
                reader = retrieveBufferedReader();
                while (reader.ready()) {
                    newContent = newContent + reader.readLine() + "\r\n";
                }
                newContent = newContent + "\r\n" + MARKED_END;

                InputStream targetStream = new ByteArrayInputStream(newContent.getBytes());

                FTPClient ftp = new FTPClient();
                ftp.connect(serverFtpIp, serverFtpPort);
                ftp.login(serverFtpUser, serverFtpPassword);
                ftp.changeWorkingDirectory("/csgo/csgo");
                ftp.storeFile("console.log", targetStream);
                ftp.completePendingCommand();
                ftp.disconnect();
            }

        } catch (SocketException ex) {
            System.out.println("Socket Exception");
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }

    private BufferedReader retrieveBufferedReader() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect(serverFtpIp, serverFtpPort);
        ftp.login(serverFtpUser, serverFtpPassword);

        ftp.changeWorkingDirectory("/csgo/csgo");
        FTPFile[] ftpFileArray = ftp.listFiles("console.log");
        InputStream inputStream = ftp.retrieveFileStream(ftpFileArray[0].getName());
        BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream));
        ftp.disconnect();

        return reader;
    }
}
