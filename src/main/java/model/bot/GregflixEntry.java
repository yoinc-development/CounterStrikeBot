package model.bot;

import java.sql.Date;

public class GregflixEntry {
    private String imdbID;
    private String title;
    private boolean uploaded;
    private Date uploadedDate;
    private String showType;


    public GregflixEntry(String imdbID, String title, boolean uploaded, Date uploadedDate, String showType) {
        this.imdbID = imdbID;
        this.title = title;
        this.uploaded = uploaded;
        this.uploadedDate = uploadedDate;
        this.showType = showType;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }
    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
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
