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

    private DataService dataService;
    private CsStatsService csStatsService;
    private CsFunService csFunService;
    private DiscordService discordService;

    public CounterStrikeBotListener(Properties properties, DataService dataService, MessageService messageService) {
        this.dataService = dataService;
        csStatsService = new CsStatsService(properties, dataService);
        csFunService = new CsFunService(messageService);
        discordService = new DiscordService(properties, dataService, messageService);
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

                if ("teams".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csFunService.handleSetTeamsEvent(event, locale).build()).queue();
                }
            }
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {

    }

    @Override
    public void onReady(ReadyEvent event){
        JDA jda = event.getJDA();
        dataService.setBotID(jda.getSelfUser().getId());
    }
}
