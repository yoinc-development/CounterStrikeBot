package listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import services.CsStatsService;
import services.RetakeService;

import java.util.Properties;

public class CounterStrikeBotListener extends ListenerAdapter {
    private CsStatsService csStatsService;
    private RetakeService retakeService;

    public CounterStrikeBotListener(Properties properties) {
        csStatsService = new CsStatsService(properties);
        retakeService = new RetakeService(properties);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String locale = "en";
        if(event.getInteraction().getUserLocale().getLocale().equals("de")) {
            locale = "de";
        }
        if ("stats".equals(event.getName())) {
            event.reply(csStatsService.handleStatsEvent(event, locale)).queue();
        }

        if ("compare".equals(event.getName())) {
            event.reply(csStatsService.handleCompareEvent(event, locale)).queue();
        }

        if ("map".equals(event.getName())) {
            event.reply(retakeService.handleMapEvent(event, locale)).queue();
        }
    }

    public CsStatsService getCsStatsService() {
        return csStatsService;
    }

    public void setCsStatsService(CsStatsService csStatsService) {
        this.csStatsService = csStatsService;
    }

    public RetakeService getRetakeService() {
        return retakeService;
    }

    public void setRetakeService(RetakeService retakeService) {
        this.retakeService = retakeService;
    }
}
