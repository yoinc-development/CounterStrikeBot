package ch.yoinc.services;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Locale;

public class DiscordService {

    public EmbedBuilder createEmbedBuilder(String title, String description, String imageUrl, String footer) {
        return new YoincEmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setImage(imageUrl)
                .setFooter(footer);
    }

    public String formatRating(Double rating) {
        if (rating == null) {
            return "n/a";
        }
        return String.format(Locale.US, "%.2f", rating * 100.0);
    }

    public static class YoincEmbedBuilder extends EmbedBuilder {
        public YoincEmbedBuilder() {
            super();
            this.setAuthor("Powered by YOINC.", "https://www.yoinc.ch");
        }
    }
}
