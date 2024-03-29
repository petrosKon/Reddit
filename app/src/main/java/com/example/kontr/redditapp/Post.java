package com.example.kontr.redditapp;

public class Post {

    private String title;
    private String author;
    private String dateUpdated;
    private String postURL;
    private String thumbnailURL;
    private String id;

    public Post(String title, String author, String dateUpdated, String postURL, String thumbnailURL, String id) {
        this.title = title;
        this.author = author;
        this.dateUpdated = dateUpdated;
        this.postURL = postURL;
        this.thumbnailURL = thumbnailURL;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getPostURL() {
        return postURL;
    }

    public void setPostURL(String postURL) {
        this.postURL = postURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
