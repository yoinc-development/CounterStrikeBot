import ch.yoinc.listeners.CounterStrikeBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class StartUp {

    private static final Logger log = LoggerFactory.getLogger(StartUp.class);

    public static void main(String[] args) {
        try {
            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            ResourceBundle resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));

            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.apiToken"))
                    .addEventListeners(new CounterStrikeBotListener(properties))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.updateCommands().addCommands(
                            Commands.slash("stats", resourceBundle.getString("command.stats.description")).addOption(OptionType.MENTIONABLE, "player", resourceBundle.getString("command.stats.value.description"), true),
                            Commands.slash("compare", resourceBundle.getString("command.compare.description")).addOption(OptionType.MENTIONABLE, "playerone", resourceBundle.getString("command.compare.valueone.description"), true).addOption(OptionType.MENTIONABLE, "playertwo", resourceBundle.getString("command.compare.valuetwo.description"), true),
                            Commands.slash("teams", resourceBundle.getString("command.teams.description")).addOption(OptionType.NUMBER, "amountofteams", resourceBundle.getString("command.teams.value.description"), false))
                    .queue();

            jda.awaitReady();
        } catch (InterruptedException | IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
