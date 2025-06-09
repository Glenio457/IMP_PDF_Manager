package com.pdfmanager.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

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

    public File getConfigPath() {
        return configPath;
    }
}
