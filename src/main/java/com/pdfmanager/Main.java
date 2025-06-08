package com.pdfmanager;

import com.pdfmanager.cli.UserInterface;
import com.pdfmanager.db.DatabaseManager;

public class Main {

    public static void main(String[] args) {
        DatabaseManager db;
        try {
            db = new DatabaseManager("/config.json");
        } catch (Exception e) {
            System.err.println("ERROR: Invalid database path");
            return;
        }
        UserInterface CLI = new UserInterface(db);

        CLI.printWelcomeMessage();
        CLI.printOptions();
    }
}