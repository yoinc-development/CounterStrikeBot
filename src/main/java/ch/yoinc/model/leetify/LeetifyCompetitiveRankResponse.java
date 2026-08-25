package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyCompetitiveRankResponse {

    @SerializedName("map_name")
    public String map_name;

    @SerializedName("rank")
    public Integer rank;

    public  LeetifyCompetitiveRankResponse() {
    }
}
