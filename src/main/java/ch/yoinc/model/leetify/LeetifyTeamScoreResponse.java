package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyTeamScoreResponse {

    @SerializedName("team_number")
    public int team_number;

    @SerializedName("score")
    public int score;

    public LeetifyTeamScoreResponse() {
    }
}
