import listeners.CounterStrikeBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class StartUp implements EventListener {

    private final DateTimeFormatter START_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");

    public static void main(String[] args) {
        try {
            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            //figure out a way to get discord guild locale if possible
            //FYI: Locale.getDefault() returns locale of OS
            ResourceBundle resourceBundle = ResourceBundle.getBundle("localization", new Locale("en"));


            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.apiToken"))
                    .addEventListeners(new StartUp())
                    .addEventListeners(new CounterStrikeBotListener(properties))
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.updateCommands().addCommands(
                    Commands.slash("map", resourceBundle.getString("command.map.description")).addOption(OptionType.STRING,"map",resourceBundle.getString("command.map.value.description"), true),
                    Commands.slash("stats", resourceBundle.getString("command.stats.description")).addOption(OptionType.STRING,"player",resourceBundle.getString("command.stats.value.description"), true),
                    Commands.slash("compare", resourceBundle.getString("command.compare.description")).addOption(OptionType.STRING, "playerone", resourceBundle.getString("command.compare.valueone.description"), true).addOption(OptionType.STRING, "playertwo", resourceBundle.getString("command.compare.valuetwo.description"), true),
                    Commands.slash("wow", resourceBundle.getString("command.wow.description")).addOption(OptionType.STRING, "url", resourceBundle.getString("command.wow.value.description"), true),
                    Commands.context(Command.Type.USER, "wow")).queue();
            jda.awaitReady();
        } catch (InterruptedException ex) {
            System.out.println("Nice. Something interrupted the connection.");
        } catch (IOException ex) {
            System.out.println("Nice. Problems with that property.");
        }
    }

    @Override
    public void onEvent(GenericEvent genericEvent) {
        if (genericEvent instanceof ReadyEvent) {
            String startMessage = LocalDateTime.now().format(START_TIME) + " - Started application.";
            System.out.println(startMessage);
        }
    }
}
