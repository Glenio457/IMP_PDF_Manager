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

    public String readField(String data) throws IOException {
        ObjectNode object = new ObjectMapper().readValue(dbPath, ObjectNode.class);
        JsonNode node = object.get(data);
        return (node == null ? null : node.textValue());
    }

    public void writeField(String field, String data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode object = mapper.readValue(dbPath, ObjectNode.class);
        object.put(field, data);
        mapper.writeValue(dbPath, object);
    }
}
