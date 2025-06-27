package com.pdfmanager.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
     * Copies a file to the library.
     * @param filePath Source path.
     * @param destination Destination path.
     */
    public void copyFileToLibrary(String filePath, String destination) {
        try {
            Files.copy(Path.of(filePath), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("ERROR: Unable to copy file to library.");
        }
    }

    /**
     * Removes a file from the library.
     * @param file The file to be removed.
     */
    public void removeFile(File file) {
        if (file.delete()) {
            System.out.println("File '" + file.getName() + "' has been deleted.");
        } else {
            System.err.println("File '" + file.getName() + "' could not be deleted.");
        }
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
