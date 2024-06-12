import listeners.CounterStrikeBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import services.FaceitMatchService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import static spark.Spark.get;
import static spark.Spark.port;

public class StartUp {

    private static final DateTimeFormatter START_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");

    public static void main(String[] args) {
        try {

            String startMessage = LocalDateTime.now().format(START_TIME) + " - Started application.";
            System.out.println(startMessage);

            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            //figure out a way to get discord guild locale if possible
            //FYI: Locale.getDefault() returns locale of OS
            ResourceBundle resourceBundle = ResourceBundle.getBundle("localization", new Locale("en"));

            FaceitMatchService faceitMatchService = new FaceitMatchService(properties);

            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.apiToken"))
                    .addEventListeners(new CounterStrikeBotListener(properties))
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.updateCommands().addCommands(
                    Commands.slash("map", resourceBundle.getString("command.map.description")).addOption(OptionType.STRING,"map",resourceBundle.getString("command.map.value.description"), true),
                    Commands.slash("stats", resourceBundle.getString("command.stats.description")).addOption(OptionType.STRING,"player",resourceBundle.getString("command.stats.value.description"), true),
                    Commands.slash("compare", resourceBundle.getString("command.compare.description")).addOption(OptionType.STRING, "playerone", resourceBundle.getString("command.compare.valueone.description"), true).addOption(OptionType.STRING, "playertwo", resourceBundle.getString("command.compare.valuetwo.description"), true),
                    Commands.slash("wow", resourceBundle.getString("command.wow.description")).addOption(OptionType.STRING, "url", resourceBundle.getString("command.wow.value.description"), true),
                    Commands.slash("teams", "description"),
                    Commands.context(Command.Type.USER, "wow")).queue();
            jda.awaitReady();

            port(50429);
            get("/data", (request, response) -> {
                faceitMatchService.receiveMatchUpdate(request);
                return null;
            });

        } catch (InterruptedException ex) {
            System.out.println("Nice. Something interrupted the connection.");
        } catch (IOException ex) {
            System.out.println("Nice. Problems with that property.");
        }
    }
}
