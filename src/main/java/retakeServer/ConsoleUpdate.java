package retakeServer;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
            System.out.println("Doing check: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            //retrieve the last line of the log
            String lastLine = "";
            while (reader.ready()) {
                lastLine = reader.readLine();
            }

            //if the last line is not the pre-defined end set by this program,
            //new lines were added since the last time the program ran
            if(!lastLine.startsWith(MARKED_END)) {
                reader = retrieveBufferedReader();
                int lineNumber = 1;
                int lastLineNumber = 1;
                while (reader.ready()) {
                    //retrieve line number of last known pre-defined end message
                    if(reader.readLine().startsWith(MARKED_END)) {
                        lastLineNumber = lineNumber;
                    }
                    lineNumber++;
                }

                //skip to last pre-defined end message and work through log messages
                reader = retrieveBufferedReader();
                lineNumber = 1;
                while (reader.ready()) {
                    String logMessage = reader.readLine();
                    if(lineNumber >= lastLineNumber) {
                        if (logMessage.startsWith("A full streak has been reached")) {
                            channel.sendMessage(logMessage).queue();
                        }
                    }
                }

                //retrieve log file content stream to add pre-defined end message
                String newContent = "";
                reader = retrieveBufferedReader();
                while (reader.ready()) {
                    newContent = newContent + reader.readLine() + "\r\n";
                }

                newContent = newContent + "\r\n" + MARKED_END;

                InputStream targetStream = new ByteArrayInputStream(newContent.getBytes());

                FTPClient ftp = retrieveFtpClient();
                ftp.storeFile("console.log", targetStream);
                ftp.completePendingCommand();
                ftp.disconnect();
            }
        } catch (IOException ex) {
            System.out.println("An IO Exception was thrown. No message was sent to the user.");
        }
    }

    private BufferedReader retrieveBufferedReader() throws IOException {
        FTPClient ftp = retrieveFtpClient();
        FTPFile[] ftpFileArray = ftp.listFiles("console.log");
        InputStream inputStream = ftp.retrieveFileStream(ftpFileArray[0].getName());
        BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream));
        ftp.disconnect();

        return reader;
    }

    private FTPClient retrieveFtpClient() throws IOException{
        FTPClient ftp = new FTPClient();
        ftp.connect(serverFtpIp, serverFtpPort);
        ftp.login(serverFtpUser, serverFtpPassword);
        ftp.changeWorkingDirectory("/csgo/csgo");

        return ftp;
    }
}
