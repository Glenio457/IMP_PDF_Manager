package com.pdfmanager.files;

import java.util.List;

public class Book extends Document {
    private String subTitle;
    private String fieldOfKnowledge;
    private int publishYear;
    private String publisher; // Campo adicionado para o BibTeX

    public Book() {
        super();
    }

    public Book(String title, String path, List<String> authors, String subTitle, String fieldOfKnowledge, int publishYear, String publisher) {
        super(title, path, authors);
        this.subTitle = subTitle;
        this.fieldOfKnowledge = fieldOfKnowledge;
        this.publishYear = publishYear;
        this.publisher = publisher; // Campo adicionado
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getFieldOfKnowledge() {
        return fieldOfKnowledge;
    }

    public void setFieldOfKnowledge(String fieldOfKnowledge) {
        this.fieldOfKnowledge = fieldOfKnowledge;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "Book{" +
                "subTitle='" + subTitle + '\'' +
                ", fieldOfKnowledge='" + fieldOfKnowledge + '\'' +
                ", publishYear=" + publishYear +
                ", publisher='" + publisher + '\'' + // Campo adicionado
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", path='" + path + '\'' +
                '}';
    }
}