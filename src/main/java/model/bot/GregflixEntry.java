package model.bot;

public class GregflixEntry {
    private String imdbID;
    private String title;
    private boolean uploaded;

    public GregflixEntry(String imdbID, String title, boolean uploaded) {
        this.imdbID = imdbID;
        this.title = title;
        this.uploaded = uploaded;
    }
    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }
}
