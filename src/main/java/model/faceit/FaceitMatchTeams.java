package model.faceit;

import com.google.gson.annotations.SerializedName;

public class FaceitMatchTeams {
    @SerializedName(value = "property1", alternate = {"faction1"})
    private FaceitMatchTeam property1;
    @SerializedName(value = "property2", alternate = {"faction2"})
    private FaceitMatchTeam property2;

    public FaceitMatchTeam getProperty1() {
        return property1;
    }

    public void setProperty1(FaceitMatchTeam property1) {
        this.property1 = property1;
    }

    public FaceitMatchTeam getProperty2() {
        return property2;
    }

    public void setProperty2(FaceitMatchTeam property2) {
        this.property2 = property2;
    }
}
