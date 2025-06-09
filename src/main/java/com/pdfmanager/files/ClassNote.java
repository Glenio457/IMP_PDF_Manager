package com.pdfmanager.files;

import java.util.List;

public class ClassNote extends Document {
    private String subTitle;
    private String lectureName;
    private String institutionName;

    public ClassNote() {
        super();
    }

    public ClassNote(String title, String path, List<String> authors, String subTitle, String lectureName, String institutionName) {
        super(title, path, authors);
        this.subTitle = subTitle;
        this.lectureName = lectureName;
        this.institutionName = institutionName;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
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
        return "ClassNote{" +
                "subTitle='" + subTitle + '\'' +
                ", lectureName='" + lectureName + '\'' +
                ", institutionName='" + institutionName + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", path='" + path + '\'' +
                '}';
    }
}
