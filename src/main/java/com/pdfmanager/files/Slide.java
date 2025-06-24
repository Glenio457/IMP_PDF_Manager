package com.pdfmanager.files;

import java.util.List;

public class Slide extends Document {
    private String lectureName;
    private String institutionName;

    public Slide() {
        super();
    }

    public Slide(String title, String path, List<String> authors, String lectureName, String institutionName) {
        super(title, path, authors);
        this.lectureName = lectureName;
        this.institutionName = institutionName;
    }

    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    @Override
    public String toString() {
        return "Slide{" +
                "lectureName='" + lectureName + '\'' +
                ", institutionName='" + institutionName + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", path='" + path + '\'' +
                '}';
    }
}
