package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyRatingResponse {

    @SerializedName("aim")
    public Double aim;

    @SerializedName("positioning")
    public Double positioning;

    @SerializedName("utility")
    public Double utility;

    @SerializedName("clutch")
    public Double clutch;

    @SerializedName("opening")
    public Double opening;

    @SerializedName("ct_leetify")
    public Double ct_leetify;

    @SerializedName("t_leetify")
    public Double t_leetify;

    public LeetifyRatingResponse() {
    }
}
