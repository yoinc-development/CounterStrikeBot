package ch.yoinc.tasks;

import ch.yoinc.http.ExternalApiConnection;
import ch.yoinc.model.internal.InternalUser;
import ch.yoinc.model.leetify.LeetifyMatchResponse;
import ch.yoinc.model.leetify.LeetifyPlayerStatsResponse;
import ch.yoinc.services.DataService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeetifyTaskTest {

    private LeetifyTask task;

    @BeforeEach
    void setUp() {
        task = new LeetifyTask();
    }

    // ---------------------------------------------------------------------
    // returnFilledEmbed
    // ---------------------------------------------------------------------

    @Test
    void returnFilledEmbed_setsTitleAndMatchUrl() throws Exception {
        MessageEmbed embed = invokeReturnFilledEmbed("New Faceit Match", Color.ORANGE,
                "players played a match", "de_dust2", "match-123", "Finished at 2026-01-01");

        assertEquals("New Faceit Match", embed.getTitle());
        assertEquals("https://leetify.com/app/match-details/match-123/overview", embed.getUrl());
    }

    @Test
    void returnFilledEmbed_setsColorDescriptionAndFooter() throws Exception {
        MessageEmbed embed = invokeReturnFilledEmbed("New Competitive Match", Color.RED,
                "some description", "de_mirage", "match-456", "Finished at 2026-01-02");

        assertEquals(Color.RED, embed.getColor());
        assertEquals("some description", embed.getDescription());
        assertEquals("Finished at 2026-01-02", embed.getFooter().getText());
    }

    @Test
    void returnFilledEmbed_buildsMapImageAndThumbnailUrlsFromMapName() throws Exception {
        MessageEmbed embed = invokeReturnFilledEmbed("New Wingman Match", Color.GREEN,
                "desc", "de_inferno", "match-789", "footer");

        assertEquals("https://raw.githubusercontent.com/MurkyYT/cs2-map-icons/main/images/thumbs/de_inferno_1_png.png",
                embed.getImage().getUrl());
        assertEquals("https://raw.githubusercontent.com/MurkyYT/cs2-map-icons/main/images/de_inferno.png",
                embed.getThumbnail().getUrl());
    }

    @Test
    void returnFilledEmbed_appliesYoincBranding() throws Exception {
        MessageEmbed embed = invokeReturnFilledEmbed("New Premier Match", Color.YELLOW,
                "desc", "de_ancient", "match-321", "footer");

        assertNotNull(embed.getAuthor());
        assertEquals("Powered by YOINC.", embed.getAuthor().getName());
    }

    // ---------------------------------------------------------------------
    // formatRating
    // ---------------------------------------------------------------------

    @Test
    void formatRating_scalesToPercentageAndRoundsToTwoDecimals() throws Exception {
        assertEquals("1.23", invokeFormatRating(0.0123));
        assertEquals("5.32", invokeFormatRating(0.0532));
        assertEquals("-0.71", invokeFormatRating(-0.0071));
    }

    @Test
    void formatRating_returnsPlaceholder_whenRatingIsMissing() throws Exception {
        assertEquals("n/a", invokeFormatRating(null));
    }

    // ---------------------------------------------------------------------
    // setNewlyPlayedMatches
    // ---------------------------------------------------------------------

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void setNewlyPlayedMatches_returnsEmptyMap_whenUserHasNoMatchHistory() throws Exception {
        DataService dataService = mock(DataService.class);
        ExternalApiConnection connection = mock(ExternalApiConnection.class);
        when(connection.getPlayerMatchHistory(eq("STEAM1"), isNull())).thenReturn(null);
        injectDependencies(dataService, connection);

        // the method sleeps 10s (real) after every user; pre-interrupting this thread makes
        // that Thread.sleep() throw immediately instead of actually waiting
        Thread.currentThread().interrupt();

        HashMap<String, List<LeetifyMatchResponse>> results = invokeSetNewlyPlayedMatches(List.of(user(1, "STEAM1")));

        assertTrue(results.isEmpty());
        verify(dataService, never()).insertAndGetNewMatches(any(), any());
        assertTrue(Thread.interrupted(), "interrupt flag should have been restored by the method");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void setNewlyPlayedMatches_onlyIncludesMatchesReturnedAsNew() throws Exception {
        DataService dataService = mock(DataService.class);
        ExternalApiConnection connection = mock(ExternalApiConnection.class);

        LeetifyMatchResponse oldMatch = match("old-match", "Alice");
        LeetifyMatchResponse newMatch = match("new-match", "Alice");
        List<LeetifyMatchResponse> history = List.of(oldMatch, newMatch);
        when(connection.getPlayerMatchHistory(eq("STEAM1"), isNull())).thenReturn(history);
        when(dataService.insertAndGetNewMatches(history, 1)).thenReturn(List.of("new-match"));
        injectDependencies(dataService, connection);

        Thread.currentThread().interrupt();

        HashMap<String, List<LeetifyMatchResponse>> results = invokeSetNewlyPlayedMatches(List.of(user(1, "STEAM1")));

        assertEquals(1, results.size());
        assertFalse(results.containsKey("old-match"));
        assertEquals(List.of(newMatch), results.get("new-match"));
        Thread.interrupted();
    }

    @Test
    @Timeout(value = 12, unit = TimeUnit.SECONDS)
    void setNewlyPlayedMatches_groupsMatchesFromDifferentUsersUnderSameMatchId() throws Exception {
        DataService dataService = mock(DataService.class);
        ExternalApiConnection connection = mock(ExternalApiConnection.class);

        LeetifyMatchResponse aliceMatch = match("shared-match", "Alice");
        LeetifyMatchResponse bobMatch = match("shared-match", "Bob");
        List<LeetifyMatchResponse> aliceHistory = List.of(aliceMatch);
        List<LeetifyMatchResponse> bobHistory = List.of(bobMatch);

        when(connection.getPlayerMatchHistory(eq("STEAM1"), isNull())).thenReturn(aliceHistory);
        when(connection.getPlayerMatchHistory(eq("STEAM2"), isNull())).thenReturn(bobHistory);
        when(dataService.insertAndGetNewMatches(aliceHistory, 1)).thenReturn(List.of("shared-match"));
        when(dataService.insertAndGetNewMatches(bobHistory, 2)).thenReturn(List.of("shared-match"));
        injectDependencies(dataService, connection);

        // the production loop sleeps 10s (real) between users, so the first sleep has to be
        // waited out for the second user to be processed at all; once it's back we interrupt
        // the worker to skip its trailing sleep instead of waiting a second 10s
        AtomicReference<HashMap<String, List<LeetifyMatchResponse>>> resultRef = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                resultRef.set(invokeSetNewlyPlayedMatches(List.of(user(1, "STEAM1"), user(2, "STEAM2"))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        worker.start();
        worker.join(10_500);
        if (worker.isAlive()) {
            worker.interrupt();
            worker.join(2_000);
        }

        HashMap<String, List<LeetifyMatchResponse>> results = resultRef.get();
        assertNotNull(results, "worker did not finish in time");
        assertEquals(1, results.size());
        assertEquals(List.of(aliceMatch, bobMatch), results.get("shared-match"));
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private MessageEmbed invokeReturnFilledEmbed(String title, Color color, String description,
                                                 String mapName, String matchId, String footer) throws Exception {
        Method method = LeetifyTask.class.getDeclaredMethod("returnFilledEmbed",
                String.class, Color.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        EmbedBuilder builder = (EmbedBuilder) method.invoke(task, title, color, description, mapName, matchId, footer);
        return builder.build();
    }

    private String invokeFormatRating(Double rating) throws Exception {
        Method method = LeetifyTask.class.getDeclaredMethod("formatRating", Double.class);
        method.setAccessible(true);
        return (String) method.invoke(null, rating);
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, List<LeetifyMatchResponse>> invokeSetNewlyPlayedMatches(List<InternalUser> users) {
        try {
            Method method = LeetifyTask.class.getDeclaredMethod("setNewlyPlayedMatches", List.class);
            method.setAccessible(true);
            return (HashMap<String, List<LeetifyMatchResponse>>) method.invoke(task, users);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectDependencies(DataService dataService, ExternalApiConnection connection) throws Exception {
        setPrivateField("dataService", dataService);
        setPrivateField("connectionBuilder", connection);
    }

    private void setPrivateField(String name, Object value) throws Exception {
        Field field = LeetifyTask.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(task, value);
    }

    private static InternalUser user(int id, String steamId) {
        InternalUser user = new InternalUser();
        user.userID = id;
        user.steamID = steamId;
        return user;
    }

    private static LeetifyMatchResponse match(String matchId, String playerName) {
        LeetifyMatchResponse match = new LeetifyMatchResponse();
        match.id = matchId;
        match.data_source = "matchmaking";
        match.map_name = "de_dust2";
        match.finished_at = Instant.now();
        LeetifyPlayerStatsResponse stats = new LeetifyPlayerStatsResponse();
        stats.name = playerName;
        match.stats = List.of(stats);
        return match;
    }
}
