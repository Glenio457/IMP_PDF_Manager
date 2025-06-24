package com.pdfmanager.files;

import java.util.List;

public class Document {
    protected String title;
    protected List<String> authors;
    protected String path;

    public Document() { }

    public Document(String title, String path, List<String> authors) {
        this.title = title;
        this.path = path;
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Document{" +
                "title='" + title + '\'' +
                ", authors=" + authors +
                ", path='" + path + '\'' +
                '}';
    }
}
