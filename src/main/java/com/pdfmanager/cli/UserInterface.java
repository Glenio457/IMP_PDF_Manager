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

    public void printOptions(DatabaseManager db) {
        // Check if this is the user's first access
        if (checkFirstAccess(db)) { // If it's the first access do:

        } else { // Otherwise do:

        }
    }

    private void userFirstAccess() {

    }

    public boolean checkFirstAccess(DatabaseManager db) {
        try {
            isFirstAccess = db.readField("isFirstAccess");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Objects.equals(isFirstAccess, "true")) {
            System.out.println("Is first access: " + isFirstAccess);
            try {
                db.writeField("isFirstAccess", "false");
            } catch (IOException e) {
                System.err.println("ERROR: Failed to update 'isFirstAccess' field in config.json");
                throw new RuntimeException(e);
            }
            return true;
        }
        System.out.println("Is first access: " + isFirstAccess);
        return false;
    }
}
