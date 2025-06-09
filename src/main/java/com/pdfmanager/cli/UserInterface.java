package com.pdfmanager.cli;

import com.pdfmanager.db.DatabaseManager;
import com.pdfmanager.utils.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserInterface {
    private String isFirstAccess;
    private String libraryPath;
    private final FileManager fileManager;
    private final DatabaseManager db;
    private final File configPath;

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
        configPath = db.getConfigPath();
        try {
            this.libraryPath = db.readField(configPath, "libraryPath");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            try {
                this.libraryPath = db.readField(configPath, "libraryPath");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }  // Otherwise do:
        // Check if file path is valid
        while(!fileManager.evaluatePath(libraryPath)) {
            System.out.println(RED + "\nLibrary directory not found" + RESET);
            editLibraryPath();
            try {
                this.libraryPath = db.readField(configPath, "libraryPath");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Scanner scanner = new Scanner(System.in);
        int input1 = -1;
        while (input1 != 0) {
            System.out.println("\nChoose one of the options below:\n" +
                    BLUE + "[1] " + RESET + "Add file\n" +
                    BLUE + "[2] " + RESET + "List files\n" +
                    BLUE + "[3] " + RESET + "Remove file\n" +
                    // Other options
                    RED + "\n[0] " + RESET + "Quit program"
            );
            input1 = scanner.nextInt();
            // TODO: Implement options
            switch(input1) {
                case 0: break;
                case 1:
                    addFile();
                    break;
                case 2: throw new UnsupportedOperationException("Not implemented yet"); //break;
                case 3: throw new UnsupportedOperationException("Not implemented yet"); //break;
                default: System.err.println("Invalid option: '" + input1 + "'"); break;
            }
        }
        System.out.println("Exiting program.");
        System.exit(0);
    }

    private void addFile() {
        Map<String, Object> buffer = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which file type you wish to add?\n" +
                BLUE + "[1] " + RESET + "Book\n" +
                BLUE + "[2] " + RESET + "Class note\n" +
                BLUE + "[3] " + RESET + "Slide\n"
        );
        // Get file type
        int input1;
        try {
            input1 = scanner.nextInt();
            // Clean stdin buffer
            scanner.nextLine();
        } catch (Exception e) {
            System.err.println("ERROR: Invalid input value. Value should be a integer.");
            System.err.flush();
            return;
        }
        // Check invalid option
        if (input1 != 1 && input1 != 2 && input1 != 3) {
            System.err.println("Invalid option: '" + input1 + "'");
            System.err.flush();
            return;
        }
        // Get title
        System.out.println("Type the title of the file: ");
        String input2;
        try {
            input2 = scanner.nextLine();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read title from input stream.");
            System.err.flush();
            return;
        }
        buffer.put("title", input2);
        // Get authors
        System.out.println("Type the name of the authors (separated by commas): ");
        try {
            input2 = scanner.nextLine();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read authors from input stream.");
            return;
        }
        List<String> authors = Arrays.asList(input2.split("\\s*,\\s*"));
        buffer.put("authors", authors);
        // Get path
        System.out.println("Type the path in which the file is located: ");
        try {
            input2 = scanner.nextLine();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read path from input stream.");
            return;
        }
        if (fileManager.evaluatePath(input2)) {
            buffer.put("path", input2);
        } else {
            System.err.println("\nPath '" + input2 + "' is not a valid path.\n");
            System.err.flush();
            return;
        }
        // Now let's define class specific fields
        if (input1 == 1) {
            buffer.put("type", "Book");
            // Get subtitle
            System.out.println("Type the book subtitle: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read subtitle from input stream.");
                return;
            }
            buffer.put("subTitle", input2);
            // Get field of knowledge
            System.out.println("Type the book field of knowledge: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read field of knowledge from input stream.");
                return;
            }
            buffer.put("fieldOfKnowledge", input2);
            // Get publish year
            System.out.println("Type the year in which the book was published: ");
            // Value of publish year needs to be an integer number as it will be converted to integer in the
            // writeObject function. If the value is not an integer, it will generate a NumberFormatException
            // which is treated by the writeObject function.
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read book publish year from input stream.");
                return;
            }
            buffer.put("publishYear", input2);
        } else if (input1 == 2) {
            buffer.put("type", "ClassNote");
        } else {
            buffer.put("type", "Slide");
        }

        if (db.writeObject(buffer)) {
            System.out.println(GREEN + "\n" + buffer.get("type") + " added successfully" + RESET);
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
     * @param field Database field
     * @param value Update value
     */
    private void updateConfig(String field, String value) {
        try {
            db.writeField(configPath, field, value);
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
            isFirstAccess = db.readField(configPath, "isFirstAccess");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Objects.equals(isFirstAccess, "true");
    }
}
