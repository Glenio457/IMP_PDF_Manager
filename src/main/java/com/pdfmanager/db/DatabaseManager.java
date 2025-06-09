package com.pdfmanager.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pdfmanager.files.Book;
import com.pdfmanager.files.ClassNote;
import com.pdfmanager.files.Slide;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DatabaseManager {

    private final File configPath;
    private final File booksPath;
    private final File slidesPath;
    private final File classNotesPath;

    public DatabaseManager() {
        this.configPath = new File(Objects.requireNonNull(getClass().getResource("/config.json")).getPath());
        this.booksPath = new File(Objects.requireNonNull(getClass().getResource("/books.json")).getPath());
        this.slidesPath = new File(Objects.requireNonNull(getClass().getResource("/slides.json")).getPath());
        this.classNotesPath = new File(Objects.requireNonNull(getClass().getResource("/classnotes.json")).getPath());
    }

    /**
     * Reads a value from a field in the database.
     * @param dbPath The database path.
     * @param field The field from which the value should be read.
     * @return Returns the value of the field if it's valid, returns <i>null</i> otherwise.
     * @throws IOException Might result in a IOException. Treatment required.
     */
    public String readField(File dbPath, String field) throws IOException {
        ObjectNode object = new ObjectMapper().readValue(dbPath, ObjectNode.class);
        JsonNode node = object.get(field);
        return (node == null ? null : node.textValue());
    }

    public List<Object> readFile(File dbPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return new ArrayList<>(Arrays.asList(mapper.readValue(dbPath, Object[].class)));
    }

    /**
     * Writes a value on a field in the database.
     * @param dbPath The database path.
     * @param field  The <i>field</i> in which the value should be written.
     * @param data   The <i>value</i> to be written.
     * @throws IOException Might result in a IOException. Treatment required.
     */
    public void writeField(File dbPath, String field, String data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode object = mapper.readValue(dbPath, ObjectNode.class);
        object.put(field, data);
        mapper.writeValue(dbPath, object);
    }

    /**
     * This function writes a Java class (<i>Book</i>, <i>Slide</i> or <i>ClassNote</i>) to their
     * specific files in the database.
     * @param buffer Is a Map of parameters the user typed in, it is used to initialize the desired class.
     *               The Map needs to contain a field <b>'type'</b> with the name of the class to be instanced.
     */
    public boolean writeObject(Map<String, Object> buffer) {
        if (!buffer.containsKey("type")) {
            System.err.println("ERROR: Missing 'type' key in Map<String, Object> buffer");
            return false;
        }

        if (buffer.get("type") == "Book") {
            addBookToDB(buffer);

        } else if (buffer.get("type") == "Slide") {
            addSlideToDB(buffer);

        } else if (buffer.get("type") == "ClassNote") {
            addClassNoteToDB(buffer);

        } else {
            System.err.println("ERROR: Invalid Document type.");
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private void addBookToDB(Map<String, Object> buffer) {
        ObjectMapper mapper = new ObjectMapper();
        List<Object> bookList;
        try {
            bookList = readFile(booksPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Book book = new Book();
        book.setTitle((String) buffer.get("title"));
        book.setPath((String) buffer.get("path"));
        book.setAuthors((List<String>) buffer.get("authors"));
        book.setSubTitle((String) buffer.get("subTitle"));
        book.setFieldOfKnowledge((String) buffer.get("fieldOfKnowledge"));
        book.setPublishYear((String) buffer.get("publishYear"));

        bookList.add(book);
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(booksPath, bookList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSlideToDB(Map<String, Object> buffer) {
        ObjectMapper mapper = new ObjectMapper();
        List<Object> slideList;
        try {
            slideList = readFile(booksPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Slide slide = new Slide();
        slide.setTitle((String) buffer.get("title"));
        slide.setPath((String) buffer.get("path"));
        slide.setAuthors((List<String>) buffer.get("authors"));
        slide.setLectureName((String) buffer.get("lectureName"));
        slide.setInstitutionName((String) buffer.get("institutionName"));

        slideList.add(slide);
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(slidesPath, slideList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addClassNoteToDB(Map<String, Object> buffer) {
        ObjectMapper mapper = new ObjectMapper();
        List<Object> classNoteList;
        try {
            classNoteList = readFile(booksPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClassNote classNote = new ClassNote();
        classNote.setTitle((String) buffer.get("title"));
        classNote.setPath((String) buffer.get("path"));
        classNote.setAuthors((List<String>) buffer.get("authors"));
        classNote.setSubTitle((String) buffer.get("subTitle"));
        classNote.setLectureName((String) buffer.get("lectureName"));
        classNote.setInstitutionName((String) buffer.get("institutionName"));

        classNoteList.add(classNote);
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(classNotesPath, classNoteList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getConfigPath() {
        return configPath;
    }
}
