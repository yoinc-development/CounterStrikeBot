package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;

public class LeetifyMatchResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("finished_at")
    public Instant finished_at;

    @SerializedName("data_source")
    public String data_source;

    @SerializedName("data_source_match_id")
    public String data_source_match_id;

    @SerializedName("map_name")
    public String map_name;

    @SerializedName("has_banned_player")
    public boolean has_banned_player;

    @SerializedName("team_scores")
    public List<LeetifyTeamScoreResponse> team_scores;

    @SerializedName("stats")
    public List<LeetifyPlayerStatsResponse> stats;

    public LeetifyMatchResponse() {
    }
}
