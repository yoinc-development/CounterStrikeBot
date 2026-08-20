package services;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class DiscordService {

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
