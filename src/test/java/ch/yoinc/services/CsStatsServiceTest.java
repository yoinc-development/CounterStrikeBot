package ch.yoinc.services;

import ch.yoinc.http.ExternalApiConnection;
import ch.yoinc.model.leetify.LeetifyProfileResponse;
import ch.yoinc.model.leetify.LeetifyRankResponse;
import ch.yoinc.model.leetify.LeetifyRatingResponse;
import ch.yoinc.model.steam.PlayerStats;
import ch.yoinc.model.steam.ResponseData;
import ch.yoinc.model.steam.SingleStat;
import ch.yoinc.model.steam.SteamUser;
import ch.yoinc.model.steam.SteamUserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsStatsServiceTest {

    private CsStatsService service;

    @BeforeEach
    void setUp() {
        service = new CsStatsService(new Properties(), mock(DataService.class));
        service.resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));
    }

    // ---------------------------------------------------------------------
    // getWingmanRankName
    // ---------------------------------------------------------------------

    @Test
    void getWingmanRankName_mapsLowestAndHighestTier() throws Exception {
        assertEquals("Silver I", invokeGetWingmanRankName(1));
        assertEquals("Global Elite", invokeGetWingmanRankName(18));
    }

    @Test
    void getWingmanRankName_returnsPlaceholder_whenOutOfRangeOrMissing() throws Exception {
        assertEquals("n/a", invokeGetWingmanRankName(null));
        assertEquals("n/a", invokeGetWingmanRankName(0));
        assertEquals("n/a", invokeGetWingmanRankName(19));
    }

    // ---------------------------------------------------------------------
    // formatFaceitRank
    // ---------------------------------------------------------------------

    @Test
    void formatFaceitRank_returnsPlaceholder_whenRanksOrFaceitMissing() throws Exception {
        assertEquals("n/a", invokeFormatFaceitRank(null));
        assertEquals("n/a", invokeFormatFaceitRank(new LeetifyRankResponse()));
    }

    @Test
    void formatFaceitRank_combinesRankAndElo() throws Exception {
        LeetifyRankResponse ranks = new LeetifyRankResponse();
        ranks.faceit = 8;
        ranks.faceit_elo = 1750;

        assertEquals("8 (1750)", invokeFormatFaceitRank(ranks));
    }

    @Test
    void formatFaceitRank_placeholdersMissingElo() throws Exception {
        LeetifyRankResponse ranks = new LeetifyRankResponse();
        ranks.faceit = 8;

        assertEquals("8 (n/a)", invokeFormatFaceitRank(ranks));
    }

    // ---------------------------------------------------------------------
    // formatNullable
    // ---------------------------------------------------------------------

    @Test
    void formatNullableInteger_returnsPlaceholder_whenNull() throws Exception {
        assertEquals("n/a", invokeFormatNullable((Integer) null));
    }

    @Test
    void formatNullableInteger_returnsValue() throws Exception {
        assertEquals("42", invokeFormatNullable(42));
    }

    @Test
    void formatNullableDouble_returnsPlaceholder_whenNull() throws Exception {
        assertEquals("n/a", invokeFormatNullable((Double) null));
    }

    @Test
    void formatNullableDouble_roundsToTwoDecimals() throws Exception {
        assertEquals("3.14", invokeFormatNullable(3.14159));
    }

    // ---------------------------------------------------------------------
    // comparePlayers
    // ---------------------------------------------------------------------

    @Test
    void comparePlayers_marksHigherStatAsWinnerWhenHigherIsBetter() throws Exception {
        ResponseData playerOne = responseData("Alice", "http://avatar/alice", 20, 5, 1, 0, 0, 100);
        ResponseData playerTwo = responseData("Bob", "http://avatar/bob", 10, 5, 1, 0, 0, 100);

        MessageEmbed embed = invokeComparePlayers(playerOne, playerTwo).build();

        assertEquals("** :star: 20 ** vs 10", fieldValue(embed, "Kills"));
    }

    @Test
    void comparePlayers_marksLowerStatAsWinnerWhenLowerIsBetter() throws Exception {
        ResponseData playerOne = responseData("Alice", "http://avatar/alice", 10, 2, 1, 0, 0, 100);
        ResponseData playerTwo = responseData("Bob", "http://avatar/bob", 10, 8, 1, 0, 0, 100);

        MessageEmbed embed = invokeComparePlayers(playerOne, playerTwo).build();

        assertEquals("** :star: 2 ** vs 8", fieldValue(embed, "Deaths"));
    }

    @Test
    void comparePlayers_reportsTie() throws Exception {
        ResponseData playerOne = responseData("Alice", "http://avatar/alice", 10, 5, 1, 0, 0, 100);
        ResponseData playerTwo = responseData("Bob", "http://avatar/bob", 10, 5, 1, 0, 0, 100);

        MessageEmbed embed = invokeComparePlayers(playerOne, playerTwo).build();

        assertEquals("10 both.", fieldValue(embed, "Kills"));
    }

    @Test
    void comparePlayers_setsWinningPlayersAvatarAsImage() throws Exception {
        ResponseData playerOne = responseData("Alice", "http://avatar/alice", 20, 5, 5, 0, 0, 200);
        ResponseData playerTwo = responseData("Bob", "http://avatar/bob", 10, 5, 1, 0, 0, 100);

        MessageEmbed embed = invokeComparePlayers(playerOne, playerTwo).build();

        assertEquals("http://avatar/alice", embed.getImage().getUrl());
    }

    @Test
    void comparePlayers_setsNoImage_whenTiedOverall() throws Exception {
        ResponseData playerOne = responseData("Alice", "http://avatar/alice", 10, 5, 1, 0, 0, 100);
        ResponseData playerTwo = responseData("Bob", "http://avatar/bob", 10, 5, 1, 0, 0, 100);

        MessageEmbed embed = invokeComparePlayers(playerOne, playerTwo).build();

        assertNull(embed.getImage());
    }

    // ---------------------------------------------------------------------
    // handleLeetifyUserContext
    // ---------------------------------------------------------------------

    @Test
    void handleLeetifyUserContext_returnsPrivacyError_whenEventHasNoTargetMember() {
        UserContextInteractionEvent event = mock(UserContextInteractionEvent.class);
        when(event.getTargetMember()).thenReturn(null);

        MessageEmbed embed = service.handleLeetifyUserContext(event).build();

        assertEquals("No stats could be loaded. (Steam Privacy Settings?)", embed.getTitle());
    }

    @Test
    void handleLeetifyUserContext_returnsNoProfileError_whenNoSteamIdLinked() throws Exception {
        DataService dataService = mock(DataService.class);
        when(dataService.getSteamIDForDiscordID("42")).thenReturn("");
        service.dataService = dataService;

        UserContextInteractionEvent event = mockEventForMember("42");

        MessageEmbed embed = service.handleLeetifyUserContext(event).build();

        assertEquals("No Leetify profile could be found for this user.", embed.getTitle());
    }

    @Test
    void handleLeetifyUserContext_fillsAllFields_forCompleteProfile() throws Exception {
        DataService dataService = mock(DataService.class);
        when(dataService.getSteamIDForDiscordID("42")).thenReturn("STEAM64");
        service.dataService = dataService;

        ExternalApiConnection connection = mock(ExternalApiConnection.class);
        when(connection.getPlayerProfile("STEAM64", null)).thenReturn(leetifyProfile());
        service.connection = connection;

        UserContextInteractionEvent event = mockEventForMember("42");

        MessageEmbed embed = service.handleLeetifyUserContext(event).build();

        assertEquals("Kaiser's Leetify Stats", embed.getTitle());
        assertEquals("7 (2100)", fieldValue(embed, "Faceit"));
        assertEquals("15000", fieldValue(embed, "Premier"));
        assertEquals("Global Elite", fieldValue(embed, "Wingman"));
        assertEquals("2.34", fieldValue(embed, "Leetify Rating"));
        assertEquals("55.00", fieldValue(embed, "Win Rate"));
        assertEquals("120", fieldValue(embed, "Played matches"));
        assertEquals("0.65", fieldValue(embed, "Aim"));
    }

    @Test
    void handleLeetifyUserContext_omitsRatingBreakdown_whenRatingAndRanksMissing() throws Exception {
        DataService dataService = mock(DataService.class);
        when(dataService.getSteamIDForDiscordID("42")).thenReturn("STEAM64");
        service.dataService = dataService;

        LeetifyProfileResponse profile = leetifyProfile();
        profile.ranks = null;
        profile.rating = null;

        ExternalApiConnection connection = mock(ExternalApiConnection.class);
        when(connection.getPlayerProfile("STEAM64", null)).thenReturn(profile);
        service.connection = connection;

        UserContextInteractionEvent event = mockEventForMember("42");

        MessageEmbed embed = service.handleLeetifyUserContext(event).build();

        assertEquals("n/a", fieldValue(embed, "Faceit"));
        assertEquals("n/a", fieldValue(embed, "Premier"));
        assertNull(findField(embed, "Aim"));
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private String invokeGetWingmanRankName(Integer wingmanRank) throws Exception {
        Method method = CsStatsService.class.getDeclaredMethod("getWingmanRankName", Integer.class);
        method.setAccessible(true);
        return (String) method.invoke(service, wingmanRank);
    }

    private String invokeFormatFaceitRank(LeetifyRankResponse ranks) throws Exception {
        Method method = CsStatsService.class.getDeclaredMethod("formatFaceitRank", LeetifyRankResponse.class);
        method.setAccessible(true);
        return (String) method.invoke(service, ranks);
    }

    private String invokeFormatNullable(Integer value) throws Exception {
        Method method = CsStatsService.class.getDeclaredMethod("formatNullable", Integer.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }

    private String invokeFormatNullable(Double value) throws Exception {
        Method method = CsStatsService.class.getDeclaredMethod("formatNullable", Double.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }

    private EmbedBuilder invokeComparePlayers(ResponseData playerOne, ResponseData playerTwo) throws Exception {
        Method method = CsStatsService.class.getDeclaredMethod("comparePlayers", ResponseData.class, ResponseData.class);
        method.setAccessible(true);
        return (EmbedBuilder) method.invoke(service, playerOne, playerTwo);
    }

    private static UserContextInteractionEvent mockEventForMember(String memberId) {
        UserContextInteractionEvent event = mock(UserContextInteractionEvent.class);
        Member targetMember = mock(Member.class);
        when(targetMember.getId()).thenReturn(memberId);
        when(event.getTargetMember()).thenReturn(targetMember);
        return event;
    }

    private static String fieldValue(MessageEmbed embed, String name) {
        return findField(embed, name).getValue();
    }

    private static MessageEmbed.Field findField(MessageEmbed embed, String name) {
        return embed.getFields().stream()
                .filter(field -> name.equals(field.getName()))
                .findFirst()
                .orElse(null);
    }

    private static ResponseData responseData(String name, String avatar, long kills, long deaths, long wins,
                                              long planted, long defused, long damage) {
        SteamUser user = new SteamUser();
        user.setPersonaname(name);
        user.setAvatarmedium(avatar);

        SteamUserInfo info = new SteamUserInfo();
        info.setPlayers(List.of(user));

        PlayerStats playerStats = new PlayerStats();
        playerStats.setStats(List.of(
                stat("total_kills", kills),
                stat("total_deaths", deaths),
                stat("total_wins", wins),
                stat("total_planted_bombs", planted),
                stat("total_defused_bombs", defused),
                stat("total_damage_done", damage)
        ));

        ResponseData data = new ResponseData();
        data.setSteamUserInfo(info);
        data.setPlayerstats(playerStats);
        return data;
    }

    private static SingleStat stat(String name, long value) {
        SingleStat stat = new SingleStat();
        stat.setName(name);
        stat.setValue(value);
        return stat;
    }

    private static LeetifyProfileResponse leetifyProfile() {
        LeetifyProfileResponse profile = new LeetifyProfileResponse();
        profile.name = "Kaiser";
        profile.winrate = 0.55;
        profile.total_matches = 120;

        LeetifyRankResponse ranks = new LeetifyRankResponse();
        ranks.faceit = 7;
        ranks.faceit_elo = 2100;
        ranks.premier = 15000;
        ranks.wingman = 18;
        ranks.leetify = 2.34;
        profile.ranks = ranks;

        LeetifyRatingResponse rating = new LeetifyRatingResponse();
        rating.ct_leetify = 0.6;
        rating.t_leetify = 0.5;
        rating.aim = 0.65;
        rating.positioning = 0.7;
        rating.utility = 0.4;
        rating.clutch = 0.3;
        rating.opening = 0.2;
        profile.rating = rating;

        return profile;
    }
}
