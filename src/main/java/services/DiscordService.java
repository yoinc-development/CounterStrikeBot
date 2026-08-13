package services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.*;

public class DiscordService {

    DataService dataService;
    Properties properties;
    JDA jda;
    ResourceBundle resourceBundle;
    MessageService messageService;

    public DiscordService(Properties properties, DataService dataService, MessageService messageService) {
        this.properties = properties;
        this.dataService = dataService;
        this.messageService = messageService;
        this.resourceBundle = ResourceBundle.getBundle("localization", new Locale("en"));
    }

    public String getUserLocale(Event event) {
        String locale = "en";
        if(event instanceof GenericCommandInteractionEvent) {
            if (((GenericCommandInteractionEvent)event).getInteraction().getUserLocale().getLocale().equals("de")) {
                locale = "de";
            }
        } else if(event instanceof ButtonInteractionEvent) {
            if(((ButtonInteractionEvent) event).getInteraction().getUserLocale().getLocale().equals("de")) {
                locale = "de";
            }
        }
        return locale;
    }
}
