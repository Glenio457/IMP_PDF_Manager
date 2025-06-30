package com.pdfmanager.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfmanager.db.DatabaseManager;
import com.pdfmanager.files.Book;
import com.pdfmanager.files.ClassNote;
import com.pdfmanager.files.Collection;
import com.pdfmanager.files.DocumentType;
import com.pdfmanager.files.Document;
import com.pdfmanager.files.Slide;
import com.pdfmanager.utils.BibTexGenerator;
import com.pdfmanager.utils.CollectionPackager;
import com.pdfmanager.utils.FileManager;
import io.restassured.path.json.JsonPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


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
        if (checkFirstAccess()) {
            System.out.println("\n$-----First access detected.-----$");
            editLibraryPath();
            try {
                this.libraryPath = db.readField(configPath, "libraryPath");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        while (!fileManager.evaluatePath(libraryPath)) {
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
                    BLUE + "[6] " + RESET + "Manage Collections\n" + // Opção Adicionada
                    RED + "\n[0] " + RESET + "Quit program"
            );
            try {
                input1 = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a number.");
                scanner.next(); // Limpa o buffer do erro
                input1 = -1; // Reseta para evitar loop infinito
                continue;
            }

            switch (input1) {
                case 0:
                    break;
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
                case 6:
                    handleCollectionsMenu(); // Método Adicionado
                    break;
                default:
                    System.err.println("Invalid option: '" + input1 + "'");
                    break;
            }
        }
        System.out.println("Exiting program.");
        System.exit(0);
    }

    // =================================================================================
    // MÉTODOS DE COLEÇÃO ADICIONADOS
    // =================================================================================

    /**
     * Exibe o submenu para gerenciamento de coleções.
     */
    private void handleCollectionsMenu() {
        Scanner scanner = new Scanner(System.in);
        int input = -1;
        while (input != 0) {
            System.out.println(BLUE + "\n--- Collections Menu ---\n" + RESET +
                    "[1] Create new collection\n" +
                    "[2] List collections\n" +
                    "[3] Add entries to a collection\n" +
                    "[4] Remove entries from a collection\n" +
                    "[5] Generate BibTeX file from a Book collection\n" +
                    "[6] Package a collection into a .zip file\n" +
                    RED + "[0] Return to main menu" + RESET
            );
            try {
                input = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Limpa o buffer do erro
                input = -1; // Reseta para evitar loop infinito
                continue;
            }

            switch (input) {
                case 1:
                    createCollection();
                    break;
                case 2:
                    listCollections();
                    break;
                case 3:
                    addEntryToCollection();
                    break;
                case 4:
                    removeEntryFromCollection();
                    break;
                case 5:
                    generateCollectionBibTex();
                    break;
                case 6:
                    packageCollection();
                    break;
                case 0:
                    break;
                default:
                    System.err.println("Invalid option.");
            }
        }
    }

    /**
     * Lógica para criar uma nova coleção.
     */
    private void createCollection() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("--- Create New Collection ---");
            System.out.print("Collection name: ");
            String name = scanner.nextLine();
            System.out.print("Author's name (must be one of the document's authors): ");
            String author = scanner.nextLine();
            System.out.print("Type (BOOK, SLIDE, or CLASS_NOTE): ");
            DocumentType type = DocumentType.valueOf(scanner.nextLine().toUpperCase());

            System.out.print("Maximum size: ");
            int maxSize = scanner.nextInt();
            scanner.nextLine();

            // Lógica para encontrar documentos elegíveis
            List<Document> eligibleDocs = findEligibleDocuments(author, type);
            if (eligibleDocs.isEmpty()) {
                System.out.println("No eligible documents found for this author and type.");
                return;
            }

            System.out.println("\nAvailable documents by " + author + ":");
            for (int i = 0; i < eligibleDocs.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, eligibleDocs.get(i).getTitle());
            }

            System.out.println("\nEnter the numbers of the documents to add, separated by commas (e.g., 1,3,4):");
            String[] selections = scanner.nextLine().split("\\s*,\\s*");
            List<String> titles = new ArrayList<>();
            for (String s : selections) {
                int index = Integer.parseInt(s.trim()) - 1;
                if (index >= 0 && index < eligibleDocs.size()) {
                    titles.add(eligibleDocs.get(index).getTitle());
                }
            }

            if (titles.size() > maxSize) {
                System.err.println("Number of selected items exceeds the maximum size of the collection.");
                return;
            }

            Collection newCollection = new Collection(name, author, type, maxSize, titles);
            db.saveCollection(newCollection);
            System.out.println(GREEN + "Collection '" + name + "' created successfully!" + RESET);

        } catch (IOException e) {
            System.err.println("Error processing documents: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid type entered. Please use BOOK, SLIDE, or CLASS_NOTE.");
        } catch (InputMismatchException e) {
            System.err.println("Invalid number format entered.");
        }
    }

    /**
     * Lista todas as coleções existentes.
     */
    private void listCollections() {
        System.out.println(BLUE + "\n--- Existing Collections ---" + RESET);
        try {
            List<Collection> collections = db.getAllCollections();
            if (collections.isEmpty()) {
                System.out.println("No collections found.");
                return;
            }
            for (Collection c : collections) {
                System.out.println(YELLOW + "#=====================================#");
                System.out.println(GREEN + "Name: " + c.getName() + RESET);
                System.out.println(BLUE + "  Author: " + c.getAuthor());
                System.out.println(BLUE + "  Type: " + c.getType());
                System.out.println(BLUE + "  Size: " + c.getEntryTitles().size() + "/" + c.getMaxSize());
                System.out.println(BLUE + "  Entries: " + c.getEntryTitles() + RESET);
            }
        } catch (IOException e) {
            System.err.println("Error reading collections: " + e.getMessage());
        }
    }

    /**
     * Adiciona entradas a uma coleção existente.
     */
    private void addEntryToCollection() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter the name of the collection to add to: ");
            String collectionName = scanner.nextLine();
            Collection collection = db.getCollectionByName(collectionName);

            if (collection == null) {
                System.err.println("Collection not found.");
                return;
            }

            if (collection.getEntryTitles().size() >= collection.getMaxSize()) {
                System.err.println("Collection is already full.");
                return;
            }

            List<Document> eligibleDocs = findEligibleDocuments(collection.getAuthor(), collection.getType());
            // Remove docs already in the collection
            eligibleDocs.removeIf(doc -> collection.getEntryTitles().contains(doc.getTitle()));

            if (eligibleDocs.isEmpty()) {
                System.out.println("No new documents available to add.");
                return;
            }

            System.out.println("\nAvailable documents to add:");
            for (int i = 0; i < eligibleDocs.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, eligibleDocs.get(i).getTitle());
            }

            System.out.println("\nEnter the numbers of the documents to add, separated by commas:");
            String[] selections = scanner.nextLine().split("\\s*,\\s*");
            List<String> titlesToAdd = new ArrayList<>();
            for (String s : selections) {
                int index = Integer.parseInt(s.trim()) - 1;
                if (index >= 0 && index < eligibleDocs.size()) {
                    titlesToAdd.add(eligibleDocs.get(index).getTitle());
                }
            }

            if (collection.getEntryTitles().size() + titlesToAdd.size() > collection.getMaxSize()) {
                System.err.println("Adding these items would exceed the collection's max size.");
                return;
            }

            collection.getEntryTitles().addAll(titlesToAdd);
            db.saveCollection(collection);
            System.out.println(GREEN + "Entries added successfully." + RESET);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Remove uma entrada de uma coleção. Se a coleção ficar vazia, ela é removida.
     */
    private void removeEntryFromCollection() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the collection to edit: ");
        String collectionName = scanner.nextLine();

        try {
            Collection collection = db.getCollectionByName(collectionName);
            if (collection == null) {
                System.err.println("Collection not found.");
                return;
            }
            System.out.println("Entries in this collection: " + collection.getEntryTitles());
            System.out.print("Enter the exact title to remove: ");
            String titleToRemove = scanner.nextLine();

            boolean removed = collection.getEntryTitles().remove(titleToRemove);

            if (removed) {
                if (collection.getEntryTitles().isEmpty()) {
                    db.deleteCollection(collection.getName());
                    System.out.println(YELLOW + "Entry removed. Collection is now empty and has been deleted." + RESET);
                } else {
                    db.saveCollection(collection);
                    System.out.println(GREEN + "Entry removed successfully." + RESET);
                }
            } else {
                System.err.println("Title not found in the collection.");
            }
        } catch (IOException e) {
            System.err.println("Error processing collection: " + e.getMessage());
        }
    }

    /**
     * Gera um arquivo BibTeX a partir de uma coleção de livros.
     */
    private void generateCollectionBibTex() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the Book collection: ");
        String collectionName = scanner.nextLine();

        try {
            Collection collection = db.getCollectionByName(collectionName);
            if (collection == null || collection.getType() != DocumentType.BOOK) {
                System.err.println("Book collection not found.");
                return;
            }
            System.out.print("Enter the full output path (e.g., C:/Users/Me/Desktop/references.bib): ");
            Path outputPath = Paths.get(scanner.nextLine());

            // CORREÇÃO: Lê o arquivo de livros usando a classe correta (Book)
            ObjectMapper mapper = new ObjectMapper();
            List<Book> allBooks = mapper.readValue(db.getBooksPath(), new TypeReference<List<Book>>() {});

            // Filtra para obter apenas os livros que estão na coleção
            List<Book> collectionBooks = allBooks.stream()
                    .filter(book -> collection.getEntryTitles().contains(book.getTitle()))
                    .collect(Collectors.toList());

            if (collectionBooks.isEmpty()) {
                System.err.println("No valid book entries found in the database for this collection.");
                return;
            }

            BibTexGenerator.generate(collection, collectionBooks, outputPath);
            System.out.println(GREEN + "BibTeX file generated successfully at " + outputPath + RESET);

        } catch (IOException e) {
            System.err.println("Error generating BibTeX file: " + e.getMessage());
            e.printStackTrace(); // Ajuda a depurar
        }
    }

    /**
     * Empacota os arquivos de uma coleção em um arquivo .zip. (VERSÃO CORRIGIDA)
     */
    private void packageCollection() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the collection name to package: ");
        String collectionName = scanner.nextLine();

        try {
            Collection collection = db.getCollectionByName(collectionName);
            if (collection == null) {
                System.err.println("Collection not found.");
                return;
            }
            System.out.print("Enter the full output path for the zip file (e.g., C:/Users/Me/Desktop/package.zip): ");
            Path outputPath = Paths.get(scanner.nextLine());

            ObjectMapper mapper = new ObjectMapper();
            List<Document> documentsToPack = new ArrayList<>();
            List<String> titles = collection.getEntryTitles();

            // CORREÇÃO: Determina o tipo de documento e o lê do arquivo JSON correto
            // usando a classe específica (Book, Slide, etc.)
            switch (collection.getType()) {
                case BOOK:
                    List<Book> allBooks = mapper.readValue(db.getBooksPath(), new TypeReference<List<Book>>() {});
                    allBooks.stream()
                            .filter(doc -> titles.contains(doc.getTitle()))
                            .forEach(documentsToPack::add);
                    break;
                case SLIDE:
                    List<Slide> allSlides = mapper.readValue(db.getSlidesPath(), new TypeReference<List<Slide>>() {});
                    allSlides.stream()
                            .filter(doc -> titles.contains(doc.getTitle()))
                            .forEach(documentsToPack::add);
                    break;
                case CLASS_NOTE:
                    List<ClassNote> allNotes = mapper.readValue(db.getClassNotesPath(), new TypeReference<List<ClassNote>>() {});
                    allNotes.stream()
                            .filter(doc -> titles.contains(doc.getTitle()))
                            .forEach(documentsToPack::add);
                    break;
            }

            if (documentsToPack.isEmpty()) {
                System.err.println("No valid document entries found in the database for this collection.");
                return;
            }

            CollectionPackager.pack(documentsToPack, outputPath);

        } catch (IOException e) {
            System.err.println("Error packaging collection: " + e.getMessage());
            e.printStackTrace(); // Ajuda a depurar
        }
    }

    /**
     * Helper to simplify user text input prompts.
     * @param message The message to display to the user.
     * @return The user's input.
     */
    private String prompt(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(message);
        return scanner.nextLine();
    }

    /**
     * Helper method to find documents eligible for adding to a collection.
     * @param author The author's name.
     * @param type The type of document.
     * @return A list of eligible documents.
     * @throws IOException
     */
    private List<Document> findEligibleDocuments(String author, DocumentType type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Document> eligibleDocs = new ArrayList<>();

        switch (type) {
            case BOOK:
                List<Book> books = mapper.readValue(db.getBooksPath(), new TypeReference<>() {});
                books.stream()
                        .filter(doc -> doc.getAuthors().contains(author))
                        .forEach(eligibleDocs::add);
                break;
            case SLIDE:
                List<Slide> slides = mapper.readValue(db.getSlidesPath(), new TypeReference<>() {});
                slides.stream()
                        .filter(doc -> doc.getAuthors().contains(author))
                        .forEach(eligibleDocs::add);
                break;
            case CLASS_NOTE:
                List<ClassNote> notes = mapper.readValue(db.getClassNotesPath(), new TypeReference<>() {});
                notes.stream()
                        .filter(doc -> doc.getAuthors().contains(author))
                        .forEach(eligibleDocs::add);
                break;
        }
        return eligibleDocs;
    }

    /**
     * Generic helper to retrieve full document objects from a collection's list of titles.
     * @param collection The collection object.
     * @param docClass The class type of the documents to retrieve (e.g., Book.class).
     * @return A list of document objects.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private <T extends Document> List<T> getDocumentsFromCollection(Collection collection, Class<T> docClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File dbPath;
        TypeReference<List<T>> typeRef;

        switch (collection.getType()) {
            case BOOK:
                dbPath = db.getBooksPath();
                typeRef = new TypeReference<>() {};
                break;
            case SLIDE:
                dbPath = db.getSlidesPath();
                typeRef = new TypeReference<>() {};
                break;
            case CLASS_NOTE:
                dbPath = db.getClassNotesPath();
                typeRef = new TypeReference<>() {};
                break;
            default:
                return new ArrayList<>();
        }

        List<T> allDocs = mapper.readValue(dbPath, typeRef);
        return allDocs.stream()
                .filter(doc -> collection.getEntryTitles().contains(doc.getTitle()))
                .collect(Collectors.toList());
    }

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

        try {
            String author = db.removeEntry(path, fileName, "authors[0]");
            if (author == null) return;
            System.out.println(GREEN + "Successfully removed '" + fileName + "' from database." + RESET);
            String filePath = db.getLibraryPath() + File.separator + author + File.separator + fileName;
            fileManager.removeFile(new File(filePath));
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

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

    private void addToLibrary(String fileName, File dbPath) throws IOException {
        String path = JsonPath.from(dbPath).get("find { it.title == '" + fileName +  "'}.path");
        String author = JsonPath.from(dbPath).get("find { it.title == '" + fileName +  "'}.authors[0]");
        fileManager.createDirectory(db.getLibraryPath(), author);
        String subDirectory = db.getLibraryPath() + File.separator + author;
        fileManager.copyFileToLibrary(path + File.separator + fileName,
                subDirectory +  File.separator + fileName);
    }

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
