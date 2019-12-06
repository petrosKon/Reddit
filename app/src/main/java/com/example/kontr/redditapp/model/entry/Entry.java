package com.example.kontr.redditapp.model.entry;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(name = "entry",strict = false)
public class Entry implements Serializable {

    @Element(required = false, name = "author")
    private Author author;

    @Element(name = "title")
    private String title;

    @Element(name = "id")
    private String id;

    @Element(name = "content")
    private String content;

    @Element(name = "updated")
    private String updated;

    public Entry() {
    }

    public Entry(Author author, String title, String content, String updated) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.updated = updated;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "\n\nEntry{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}
