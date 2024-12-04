package model;

public class RatedSeries {

    private String title;

    private int rating;

    private String lastUpdated;

    private static boolean isRated;

    public RatedSeries(String title, int rating, String lastUpdated) {
        this.title = title;
        this.rating = rating;
        this.lastUpdated = lastUpdated;
        this.isRated = false;
    }

    public static boolean isRated(String title) {
        return isRated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
        isRated = true;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}