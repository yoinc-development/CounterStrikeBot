package listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import services.*;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class CounterStrikeBotListener extends ListenerAdapter {

    private CsStatsService csStatsService;
    private RetakeService retakeService;
    private CsFunService csFunService;
    private DiscordService discordService;

    public CounterStrikeBotListener(Properties properties, DataService dataService, MessageService messageService) {
        csStatsService = new CsStatsService(properties, dataService);
        csFunService = new CsFunService(dataService, messageService);
        retakeService = new RetakeService(properties, dataService, messageService);
        discordService = new DiscordService(properties, dataService, retakeService, messageService);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

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

                if ("map".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessage(retakeService.handleMapEvent(event, locale)).queue();
                }

                if ("wow".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessage(csFunService.handleAddWowEvent(event, locale)).queue();
                }

                if ("teams".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csFunService.handleSetTeamsEvent(event, locale).build()).queue();
                }

                if ("status".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(retakeService.handleStatusEvent(event, locale).build()).queue();
                }
            }
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
        String locale = discordService.getUserLocale(event);

        if("wow".equals(event.getName())) {
            event.deferReply().queue();
            event.getHook().sendMessage(csFunService.handleWowEvent(event, locale)).queue();
        }
        if("retake stats".equals(event.getName())){
            event.deferReply().queue();
            event.getHook().sendMessageEmbeds(retakeService.handleStatsEvent(event, locale).build()).queue();
        }
    }

    @Override
    public void onReady(ReadyEvent event){
        JDA jda = event.getJDA();
        CompletableFuture.runAsync( () -> discordService.scheduleAllTasks(jda));
    }
}
