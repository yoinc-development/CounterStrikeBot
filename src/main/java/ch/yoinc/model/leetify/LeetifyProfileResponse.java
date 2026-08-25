package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;

public class LeetifyProfileResponse {

    @SerializedName("privacy_mode")
    public String privacy_mode;

    @SerializedName("winrate")
    public Double winrate;

    @SerializedName("total_matches")
    public Integer total_matches;

    @SerializedName("first_match_date")
    public Instant first_match_date;

    @SerializedName("name")
    public String name;

    @SerializedName("bans")
    public List<LeetifyBanResponse> bans;

    @SerializedName("steam64_id")
    public String steam64_id;

    @SerializedName("id")
    public String id;

    @SerializedName("ranks")
    public LeetifyRankResponse ranks;

    @SerializedName("rating")
    public LeetifyRatingResponse rating;

    @SerializedName("recent_teammates")
    public List<LeetifyRecentTeammatesResponse> recent_teammates;

    public LeetifyProfileResponse() {
    }
}
