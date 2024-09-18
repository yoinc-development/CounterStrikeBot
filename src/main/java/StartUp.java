import listeners.CounterStrikeBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import services.DataService;
import services.FaceitMatchService;
import services.MessageService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
            DataService dataService = null;
            MessageService messageService = null;
            try {
                dataService = new DataService(properties);
                messageService = new MessageService(properties);
            } catch (SQLException ex) {
                System.out.println("SQL Exception thrown: " + ex.getMessage());
            }

            FaceitMatchService faceitMatchService = new FaceitMatchService(properties, dataService);

            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.apiToken"))
                    .addEventListeners(new CounterStrikeBotListener(properties, dataService, messageService))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.updateCommands().addCommands(
                    Commands.slash("map", resourceBundle.getString("command.map.description")).addOption(OptionType.STRING,"map",resourceBundle.getString("command.map.value.description"), true),
                    Commands.slash("stats", resourceBundle.getString("command.stats.description")).addOption(OptionType.STRING,"player",resourceBundle.getString("command.stats.value.description"), true),
                    Commands.slash("compare", resourceBundle.getString("command.compare.description")).addOption(OptionType.STRING, "playerone", resourceBundle.getString("command.compare.valueone.description"), true).addOption(OptionType.STRING, "playertwo", resourceBundle.getString("command.compare.valuetwo.description"), true),
                    Commands.slash("wow", resourceBundle.getString("command.wow.description")).addOption(OptionType.STRING, "url", resourceBundle.getString("command.wow.value.description"), true),
                    Commands.slash("teams", resourceBundle.getString("command.teams.description")).addOption(OptionType.NUMBER, "amountofteams", resourceBundle.getString("command.teams.value.description"), false),
                    Commands.slash("status", resourceBundle.getString("command.status.description")),
                    Commands.context(Command.Type.USER, "wow"),
                    Commands.context(Command.Type.USER, "retake stats")).queue();

            jda.awaitReady();

            port(50429);
            get("/faceit-match-started", (request, response) -> {
                faceitMatchService.handleFaceitMatchStartEvent(request, jda.getGuilds());
                return response;
            });
            get("/faceit-match-ended", (request, response) -> {
                faceitMatchService.handleFaceitMatchEndEvent(request, jda.getGuilds());
                return response;
            });
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException thrown: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException thrown: " + ex.getMessage());
        }
    }
}
