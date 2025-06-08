package com.pdfmanager.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DatabaseManager {

    File dbPath;

    public DatabaseManager(String database) {
        this.dbPath = new File(Objects.requireNonNull(getClass().getResource(database)).getPath());
    }

    public String readField(String name) throws IOException {
        ObjectNode object = new ObjectMapper().readValue(dbPath, ObjectNode.class);
        JsonNode node = object.get(name);
        return (node == null ? null : node.textValue());
    }
}
