package com.pdfmanager.cli;

import com.pdfmanager.db.DatabaseManager;

import java.io.IOException;
import java.util.Objects;

public class UserInterface {
    private String input;
    private String isFirstAccess;

    public UserInterface() {
        this.input = "";
        this.isFirstAccess = "true";
    }

    public void printWelcomeMessage() {
        System.out.println("#================================#");
        System.out.println("|    Welcome to PDF Manager!!    |");
        System.out.println("#================================#");
    }

    public boolean checkFirstAccess(DatabaseManager db) {
        try {
            isFirstAccess = db.readField("isFirstAccess");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Objects.equals(isFirstAccess, "true")) {
            System.out.println(isFirstAccess);
            return true;
        }
        System.out.println(isFirstAccess);
        return false;
    }
}
