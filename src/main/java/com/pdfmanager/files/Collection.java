package com.pdfmanager.files;

import java.util.List;

public class Collection {
    private String name;
    private String author;
    private DocumentType type;
    private int maxSize;
    private List<String> entryTitles; // Armazena apenas os títulos dos documentos

    // Construtor padrão para a desserialização do Jackson
    public Collection() {
    }

    public Collection(String name, String author, DocumentType type, int maxSize, List<String> entryTitles) {
        this.name = name;
        this.author = author;
        this.type = type;
        this.maxSize = maxSize;
        this.entryTitles = entryTitles;
    }

    // Getters e Setters para todos os campos
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getEntryTitles() {
        return entryTitles;
    }

    public void setEntryTitles(List<String> entryTitles) {
        this.entryTitles = entryTitles;
    }

    @Override
    public String toString() {
        return "Collection{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", type=" + type +
                ", maxSize=" + maxSize +
                ", entryTitles=" + entryTitles +
                '}';
    }
}