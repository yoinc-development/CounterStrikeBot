package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LeetifyRankResponse {

    @SerializedName("leetify")
    public Double leetify;

    @SerializedName("premier")
    public Integer premier;

    @SerializedName("faceit")
    public Integer faceit;

    @SerializedName("faceit_elo")
    public Integer faceit_elo;

    @SerializedName("wingman")
    public Integer wingman;

    @SerializedName("renown")
    public Integer renown;

    @SerializedName("competitive")
    public List<LeetifyCompetitiveRankResponse> competitive;

    public LeetifyRankResponse() {
    }
}
