package listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import services.*;

import java.util.Properties;

public class CounterStrikeBotListener extends ListenerAdapter {

    private final DataService dataService;
    private final CsStatsService csStatsService;
    private final CsFunService csFunService;
    private final DiscordService discordService;

    public CounterStrikeBotListener(Properties properties) {
        dataService = new DataService(properties);
        csStatsService = new CsStatsService(properties, dataService);
        csFunService = new CsFunService(new MessageService(properties));
        discordService = new DiscordService();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        String locale = discordService.getUserLocale(event);

        if(!event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            if (event.getGuild() != null && event.getGuild().getMembers().contains(event.getMember())) {
                if ("stats".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csStatsService.handleStatsEvent(event, locale).build()).queue();
                }

                if ("compare".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csStatsService.handleCompareEvent(event, locale).build()).queue();
                }

                if ("teams".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csFunService.handleSetTeamsEvent(event, locale).build()).queue();
                }
            }
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {

    }

    @Override
    public void onReady(ReadyEvent event){
        JDA jda = event.getJDA();
        dataService.setBotID(jda.getSelfUser().getId());
    }
}
