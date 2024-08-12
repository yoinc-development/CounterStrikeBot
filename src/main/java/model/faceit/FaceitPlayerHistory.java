package model.faceit;

import java.util.List;

public class FaceitPlayerHistory {
    private int end;
    private int from;
    private int start;
    private int to;
    private List<FaceitMatch> items;

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public List<FaceitMatch> getItems() {
        return items;
    }

    public void setItems(List<FaceitMatch> items) {
        this.items = items;
    }
}
