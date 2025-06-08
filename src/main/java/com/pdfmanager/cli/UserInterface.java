package com.pdfmanager.cli;

import com.pdfmanager.db.DatabaseManager;
import com.pdfmanager.utils.files.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class UserInterface {
    private String isFirstAccess;
    private final FileManager fileManager;
    private final DatabaseManager db;

    // Colored text constants
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

    public UserInterface(DatabaseManager db) {
        this.isFirstAccess = "true";
        this.fileManager = new FileManager();
        this.db = db;
    }

    /**
     * Prints the PDF Manager welcome message.
     */
    public void printWelcomeMessage() {
        System.out.println("#================================#");
        System.out.println("|    Welcome to PDF Manager!!    |");
        System.out.println("#================================#");
    }

    /**
     * Prints the available options to the user.
     */
    public void printOptions() {
        // Check if this is the user's first access
        if (checkFirstAccess()) { // If it's the first access do:
            System.out.println("\n$-----First access detected.-----$");
            editLibraryPath();
        } else { // Otherwise do:
            System.out.println("Success!!");
        }
    }

    /**
     * Edits the default library path
     */
    private void editLibraryPath() {
        System.out.println("\nPlease specify a valid path for your library:");
        Scanner scanner = new Scanner(System.in);
        String input1 = scanner.nextLine();

        while(!fileManager.evaluatePath(input1)) {
            System.out.println(RED + "\nPath '" + input1 + "' is not a valid path.\n" + RESET +
                    "Please specify a valid path for your library: ");
            input1 = scanner.nextLine();
        }

        System.out.println(GREEN + "\nCreating 'library' directory in path: " + BLUE + input1 + RESET);

        if (fileManager.createDirectory(input1, "library")) {
            updateConfig("libraryPath", input1 + File.separator + "library");
            updateConfig("isFirstAccess", "false");
        } else {
            System.out.println(RED + "Failed to create 'library' directory. The directory may already exist.\n" +
                    RESET + "Do you want to use the pre-existing 'library' directory as the default path?\n" +
                    "Keep in mind that any data present in the 'library' might be altered." + BLUE + "(y/n)" + RESET
            );
            String input2 = scanner.nextLine();
            if (Objects.equals(input2, "yes") || Objects.equals(input2, "y")) {
                updateConfig("libraryPath", input1 + File.separator + "library");
                updateConfig("isFirstAccess", "false");

            } else if (Objects.equals(input2, "no") || Objects.equals(input2, "n")) {
                editLibraryPath();
            } else {
                System.err.println("Unknown option: '" + input2 + "'");
                editLibraryPath();
            }
        }
    }

    /**
     * Updates a database field with a specific value
     *
     * @param field Database field
     * @param value Update value
     */
    private void updateConfig(String field, String value) {
        try {
            db.writeField(field, value);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to update '" + field + "' field in config.json");
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if it's the first time the user is executing the program
     *
     * @return Returns <i>true</i> if it's the first time and <i>false</i> otherwise
     */
    public boolean checkFirstAccess() {
        try {
            isFirstAccess = db.readField("isFirstAccess");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Objects.equals(isFirstAccess, "true")) {
            System.out.println("Is first access: " + isFirstAccess);
            return true;
        }
        System.out.println("Is first access: " + isFirstAccess);
        return false;
    }
}
