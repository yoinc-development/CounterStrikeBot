import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class StartUp implements EventListener {

    private static Logger LOG = LoggerFactory.getLogger(StartUp.class);
    private final DateTimeFormatter START_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");

    public static void main(String[] args) {
        try {
            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.apiToken"))
                    .addEventListeners(new StartUp())
                    .addEventListeners(new RetakeMessage(properties))
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.awaitReady();
        } catch (LoginException ex) {
            LOG.error("Nice. Login failed.");
        } catch (InterruptedException ex) {
            LOG.error("Nice. Something interrupted the connection.");
        } catch (IOException ex) {
            LOG.error("Nice. Problems with that property.");
        }
    }

    @Override
    public void onEvent(@Nonnull GenericEvent genericEvent) {
        if(genericEvent instanceof ReadyEvent) {
            String startMessage = LocalDateTime.now().format(START_TIME) + " - Started application.";
            System.out.println(startMessage);
            LOG.info(startMessage);
        }
    }
}
