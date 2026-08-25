package ch.yoinc.services;

import net.dv8tion.jda.api.EmbedBuilder;
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

    public EmbedBuilder createEmbedBuilder(String title, String description, String imageUrl, String footer) {
        return new YoincEmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setImage(imageUrl)
                .setFooter(footer);
    }

    public static class YoincEmbedBuilder extends EmbedBuilder {
        public YoincEmbedBuilder() {
            super();
            this.setAuthor("Powered by YOINC.", "https://www.yoinc.ch");
        }
    }
}
