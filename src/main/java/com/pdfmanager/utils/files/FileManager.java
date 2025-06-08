package com.pdfmanager.utils.files;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {

    /**
     * Checks if a path is valid.
     * @param input Path to be checked.
     * @return Returns <i>true</i> if <b>input</b> is a valid path and <i>false</i> otherwise.
     */
    public boolean evaluatePath(String input) {
        Path path = Paths.get(input);
        return Files.exists(path) && Files.isDirectory(path);
    }

    /**
     * Creates a directory with the specified parameters.
     * @param path Path in which the directory should be created.
     * @param dirName The name of the directory.
     * @return Returns <i>true</i> if the directory was successfully created and <i>false</i> otherwise.
     */
    public boolean createDirectory(String path, String dirName) {
        File directory = new File(path + File.separator + dirName);
        return directory.mkdir();
    }
}
