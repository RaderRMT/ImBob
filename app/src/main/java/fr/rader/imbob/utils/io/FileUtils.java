package fr.rader.imbob.utils.io;

import java.io.File;
import java.io.IOException;

import fr.rader.imbob.windows.impl.LoggerWindow;

public class FileUtils {

    /**
     * Create a file
     *
     * @param path The path of the file to create
     * @param overwrite true if we want to overwrite the file, false otherwise
     * @return The file that was created, or null if it cannot be created
     */
    public static File makeFile(String path, boolean overwrite) {
        return makeFile(new File(path), overwrite);
    }

    /**
     * Create a file
     *
     * @param file The file to create
     * @param overwrite true if we want to overwrite the file, false otherwise
     * @return The file that was created, or null if it cannot be created
     */
    public static File makeFile(File file, boolean overwrite) {
        LoggerWindow.info("Creating file " + file.getAbsolutePath());

        // deleting the file if it already exists
        if (overwrite) {
            if (file.exists()) {
                LoggerWindow.info(file.getAbsolutePath() + " already exists, deleting it");

                if (!file.delete()) {
                    LoggerWindow.error("Couldn't delete " + file.getAbsolutePath());
                    return null;
                }
            }
        }

        // create the parent directory if it does not exist
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                return null;
            }
        }

        // creating the file
        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // returning the newly-created file
        return file;
    }
}
