package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyTeamScoreResponse {

    @SerializedName("team_number")
    private int team_number;

    @SerializedName("score")
    private int score;

    public LeetifyTeamScoreResponse() {
    }
}
