package com.pdfmanager;

import com.pdfmanager.cli.UserInterface;
import com.pdfmanager.db.DatabaseManager;

public class Main {

    public static void main(String[] args) {
        DatabaseManager db;
        UserInterface CLI = new UserInterface();
        try {
            db = new DatabaseManager("/database.json");
        } catch (Exception e) {
            System.err.println("ERROR: Invalid database path");
            return;
        }

        System.out.println("Hello World!");
        CLI.checkFirstAccess(db);
    }
}