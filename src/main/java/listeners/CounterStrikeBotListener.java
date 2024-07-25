package listeners;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import services.CsFunService;
import services.CsStatsService;
import services.DataService;
import services.RetakeService;

import java.util.Properties;

public class CounterStrikeBotListener extends ListenerAdapter {

    private CsStatsService csStatsService;
    private RetakeService retakeService;
    private CsFunService csFunService;

    public CounterStrikeBotListener(Properties properties, DataService dataService) {
        csStatsService = new CsStatsService(properties, dataService);
        csFunService = new CsFunService(properties, dataService);
        retakeService = new RetakeService(properties);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String locale = getUserLocale(event);

        if ("stats".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessageEmbeds(csStatsService.handleStatsEvent(event, locale).build()).queue();
        }

        if ("compare".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessageEmbeds(csStatsService.handleCompareEvent(event, locale).build()).queue();
        }

        if ("map".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessage(retakeService.handleMapEvent(event, locale)).queue();
        }

        if("wow".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessage(csFunService.handleAddWowEvent(event, locale)).queue();
        }

        if("teams".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessageEmbeds(csFunService.handleSetTeamsEvent(event, locale).build()).queue();
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {

        /*
        why is this bad?

        if an enacting user wow's a target user, the message will be displayed in the
        language of the enacting user. as a "neutral" locale the guild could be used
        (event.getGuild().getLocale()) but it could result in the same problem.
         */
        String locale = getUserLocale(event);

        if("wow".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessage(csFunService.handleWowEvent(event, locale)).queue();
        }

    }

    protected String getUserLocale(GenericCommandInteractionEvent event) {
        String locale = "en";
        if(event.getInteraction().getUserLocale().getLocale().equals("de")) {
            locale = "de";
        }
        return locale;
    }
}
