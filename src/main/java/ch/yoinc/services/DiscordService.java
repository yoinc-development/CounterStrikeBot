package ch.yoinc.services;

import net.dv8tion.jda.api.EmbedBuilder;

public class DiscordService {

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
