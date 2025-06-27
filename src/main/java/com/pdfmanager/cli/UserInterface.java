package com.pdfmanager.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfmanager.db.DatabaseManager;
import com.pdfmanager.files.Book;
import com.pdfmanager.files.ClassNote;
import com.pdfmanager.files.Slide;
import com.pdfmanager.utils.FileManager;
import io.restassured.path.json.JsonPath;

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
            try {
                System.out.println(GREEN + "\nCurrent library: " + BLUE + db.getLibraryPath() + RESET);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("\nChoose one of the options below:\n" +
                    BLUE + "[1] " + RESET + "Add file\n" +
                    BLUE + "[2] " + RESET + "List files\n" +
                    BLUE + "[3] " + RESET + "Remove file\n" +
                    BLUE + "[4] " + RESET + "Change library\n" +
                    BLUE + "[5] " + RESET + "Edit entry\n" +
                    // Other options
                    RED + "\n[0] " + RESET + "Quit program"
            );
            input1 = scanner.nextInt();
            switch(input1) {
                case 0: break;
                case 1:
                    addFile();
                    break;
                case 2:
                    listFiles();
                    break;
                case 3:
                    removeFile();
                    break;
                case 4:
                    editLibraryPath();
                    break;
                case 5:
                    editField();
                    break;
                default: System.err.println("Invalid option: '" + input1 + "'"); break;
            }
        }
        System.out.println("Exiting program.");
        System.exit(0);
    }

    /**
     * Handles the remove file option logic.
     */
    private void removeFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which file type you wish to remove?\n" +
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
        System.out.println("Type the name of the file you wish to remove.");
        String fileName;
        try {
            fileName = scanner.nextLine();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read title from input stream.");
            System.err.flush();
            return;
        }
        File path;

        if (input1 == 1) path = db.getBooksPath();
        else if (input1 == 2) path = db.getClassNotesPath();
        else path = db.getSlidesPath();

        // Tries to remove the entry based on the gathered information.
        try {
            // The first author is rescued from the removed entry so that the physical file can be
            // located and deleted from the library (the file is inside a directory name by author[0]).
            String author = db.removeEntry(path, fileName, "authors[0]");
            if (author == null) return;
            System.out.println(GREEN + "Successfully removed '" + fileName + "' from database." + RESET);
            String filePath = db.getLibraryPath() + File.separator + author + File.separator + fileName;
            fileManager.removeFile(new File(filePath));
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

    }

    /**
     * Handles the add file option logic.
     */
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
            // Get subtitle
            System.out.println("Type the class note subtitle: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read subtitle from input stream.");
                return;
            }
            buffer.put("subTitle", input2);
            // Get lecture name
            System.out.println("Type the lecture name: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read lecture name from input stream.");
                return;
            }
            buffer.put("lectureName", input2);
            // Get institution name
            System.out.println("Type the institution name: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read institution name from input stream.");
                return;
            }
            buffer.put("institutionName", input2);
        } else {
            buffer.put("type", "Slide");
            // Get lecture name
            System.out.println("Type the lecture name: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read lecture name from input stream.");
                return;
            }
            buffer.put("lectureName", input2);
            // Get institution name
            System.out.println("Type the institution name: ");
            try {
                input2 = scanner.nextLine();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to read institution name from input stream.");
                return;
            }
            buffer.put("institutionName", input2);
        }

        // Tries to write the json object in the database, if successful, copies file to library.
        if (db.writeObject(buffer)) {
            String title = buffer.get("title").toString();
            String type = buffer.get("type").toString();
            File path;
            if (type.equals("Book")) path = db.getBooksPath();
            else if (type.equals("ClassNote")) path = db.getClassNotesPath();
            else path = db.getSlidesPath();
            try {
                addToLibrary(title, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(GREEN + "\n" + type + " added successfully" + RESET);
        }
    }

    /**
     * Handles the edit field option logic.
     */
    private void editField() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which file type you wish to edit?\n" +
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
        System.out.println("Type the name of the file you wish to edit.");
        String fileName;
        try {
            fileName = scanner.nextLine();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to read title from input stream.");
            System.err.flush();
            return;
        }
        File path;

        if (input1 == 1) path = db.getBooksPath();
        else if (input1 == 2) path = db.getClassNotesPath();
        else path = db.getSlidesPath();

        try {
            db.editFieldByTitle(path, fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies a specific file from its path in the host computer to the library.
     * @param fileName The name of the file to be copied (with extension).
     * @param dbPath The database path.
     * @throws IOException Might throw an exception if unable to manipulate files.
     */
    private void addToLibrary(String fileName, File dbPath) throws IOException {
        String path = JsonPath.from(dbPath).get("find { it.title == '" + fileName +  "'}.path");
        String author = JsonPath.from(dbPath).get("find { it.title == '" + fileName +  "'}.authors[0]");
        fileManager.createDirectory(db.getLibraryPath(), author);
        String subDirectory = db.getLibraryPath() + File.separator + author;
        fileManager.copyFileToLibrary(path + File.separator + fileName,
                subDirectory +  File.separator + fileName);
    }

    /**
     * Handles the list files option.
     */
    private void listFiles() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("What files do you want to list?\n" +
                BLUE + "[1] " + RESET + "Books\n" +
                BLUE + "[2] " + RESET + "Class notes\n" +
                BLUE + "[3] " + RESET + "Slides\n" +
                BLUE + "[4] " + RESET + "All\n"
        );
        // Get listing option
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
        if (input1 != 1 && input1 != 2 && input1 != 3 && input1 != 4) {
            System.err.println("Invalid option: '" + input1 + "'");
            System.err.flush();
            return;
        }

        if (input1 == 1) {
            printBooks();
        }
        else if (input1 == 2) {
            printClassNotes();
        }
        else if (input1 == 3) {
            printSlides();
        }
        else {
            printBooks();
            printClassNotes();
            printSlides();
        }
    }

    /**
     * Prints books from the database.
     */
    private void printBooks() {
        ObjectMapper mapper = new ObjectMapper();
        List<Book> bookList;
        try {
            bookList = mapper.readValue(db.getBooksPath(), new  TypeReference<>() {});
        } catch (IOException e) {
            System.err.println("ERROR: Failed to read database.");
            System.err.flush();
            return;
        }

        for (Book book : bookList) {
            System.out.println(YELLOW + "#===============================================================#");
            System.out.println(GREEN + "type: Book" + RESET);
            System.out.println(BLUE + "title: " + book.getTitle() + RESET);
            System.out.println(BLUE + "subTitle: " + book.getSubTitle() + RESET);
            System.out.println(BLUE + "authors: " + book.getAuthors() + RESET);
            System.out.println(BLUE + "fieldOfKnowledge: " + book.getFieldOfKnowledge() + RESET);
            System.out.println(BLUE + "publishYear: " + book.getPublishYear() + RESET);
            System.out.println(BLUE + "path: " + book.getPath() + RESET);
        }
    }

    /**
     * Prints class notes from the database.
     */
    private void printClassNotes() {
        ObjectMapper mapper = new ObjectMapper();
        List<ClassNote> classNoteList;
        try {
            classNoteList = mapper.readValue(db.getClassNotesPath(), new  TypeReference<>() {});
        } catch (IOException e) {
            System.err.println("ERROR: Failed to read database.");
            System.err.flush();
            return;
        }

        for (ClassNote classNote : classNoteList) {
            System.out.println(YELLOW + "#===============================================================#");
            System.out.println(GREEN + "type: Class note" + RESET);
            System.out.println(BLUE + "title: " + classNote.getTitle() + RESET);
            System.out.println(BLUE + "subTitle: " + classNote.getSubTitle() + RESET);
            System.out.println(BLUE + "authors: " + classNote.getAuthors() + RESET);
            System.out.println(BLUE + "lectureName: " + classNote.getLectureName() + RESET);
            System.out.println(BLUE + "institutionName: " + classNote.getInstitutionName() + RESET);
            System.out.println(BLUE + "path: " + classNote.getPath() + RESET);
        }
    }

    /**
     * Prints slides from the database.
     */
    private void printSlides() {
        ObjectMapper mapper = new ObjectMapper();
        List<Slide> slideList;
        try {
            slideList = mapper.readValue(db.getSlidesPath(), new  TypeReference<>() {});
        } catch (IOException e) {
            System.err.println("ERROR: Failed to read database.");
            System.err.flush();
            return;
        }

        for (Slide slide : slideList) {
            System.out.println(YELLOW + "#===============================================================#");
            System.out.println(GREEN + "type: Slide" + RESET);
            System.out.println(BLUE + "title: " + slide.getTitle() + RESET);
            System.out.println(BLUE + "authors: " + slide.getAuthors() + RESET);
            System.out.println(BLUE + "lectureName: " + slide.getLectureName() + RESET);
            System.out.println(BLUE + "institutionName: " + slide.getInstitutionName() + RESET);
            System.out.println(BLUE + "path: " + slide.getPath() + RESET);
        }
    }

    /**
     * Edits the default library path.
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

        System.out.println("\nPlease specify a name for the library:");
        String libraryName = scanner.nextLine();

        System.out.println(GREEN + "\nCreating '" + libraryName + "' directory in path: " + BLUE + input1 + RESET);

        if (fileManager.createDirectory(input1, libraryName)) {
            updateConfig("libraryPath", input1 + File.separator + libraryName);
            updateConfig("isFirstAccess", "false");
        } else {
            System.out.println(RED + "Failed to create '" + libraryName + "' directory. The directory may already exist.\n" +
                    RESET + "Do you want to use '" + libraryName + "' as the default path?\n" +
                    "Keep in mind that any data present in '" + libraryName + "' might be altered." + BLUE + "(y/n)" + RESET
            );
            String input2 = scanner.nextLine();
            if (Objects.equals(input2, "yes") || Objects.equals(input2, "y")) {
                updateConfig("libraryPath", input1 + File.separator + libraryName);
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
     * Updates a database field with a specific value.
     * @param field Database field.
     * @param value Update value.
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
     * Check if it's the first time the user is executing the program.
     * @return Returns <i>true</i> if it's the first time and <i>false</i> otherwise.
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
