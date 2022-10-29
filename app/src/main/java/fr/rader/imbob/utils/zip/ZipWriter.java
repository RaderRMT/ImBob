package fr.rader.imbob.utils.zip;

import fr.rader.imbob.utils.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriter {

    /** Buffer size for the {@link ZipOutputStream#write(byte[], int, int)} method */
    private static final int BUFFER_SIZE = 8192;

    /** The ZIP file we're creating */
    private final File zipFile; // this is our zip file

    private final List<String> entriesName;

    /** Various OutputStreams to make this work */
    private FileOutputStream zipOutputStream; // this OutputStream is used for the ZipOutputStream

    private ZipOutputStream outputStream; // this is the OutputStream we're using to write ZIP entries

    /**
     * Allows us to write ZIP files
     *
     * @param path Path to the ZIP file to write
     */
    public ZipWriter(String path) throws IOException {
        this(new File(path));
    }

    /**
     * Allows us to write ZIP files
     *
     * @param file ZIP file to write
     */
    public ZipWriter(File file) throws IOException {
        this.entriesName = new ArrayList<>();
        this.zipFile = FileUtils.makeFile(file, true);

        // stop creating the ZipWriter as the zip file wasn't created
        if (this.zipFile == null) {
            return;
        }

        // open the streams
        this.zipOutputStream = new FileOutputStream(this.zipFile);
        this.outputStream = new ZipOutputStream(this.zipOutputStream);
    }

    /**
     * Add a new entry to the zip file
     *
     * @param file File to write in the ZIP
     * @throws IOException If an I/O error occurs
     */
    public void addFile(File file) throws IOException {
        // return if the file does not exist,
        // why write a file that doesn't exist?
        if (!file.exists()) {
            return;
        }

        // adding the entry in the zip
        addEntry(file.getName(), new FileInputStream(file));
    }

    /**
     * Add a new entry to the zip file and close the input stream
     *
     * @param name The name of the entry
     * @param stream The stream containing the file's data
     * @throws IOException If an I/O error occurs
     */
    public void addEntry(String name, InputStream stream) throws IOException {
        // return if the zipFile is null, aka if it wasn't created
        // why write in a file that doesn't exist?
        if (this.zipFile == null) {
            return;
        }

        // we add the entry name to the entry name list
        this.entriesName.add(name);

        // creating the entry
        ZipEntry entry = new ZipEntry(name);
        this.outputStream.putNextEntry(entry);

        // writing the file to the zip
        int length;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((length = stream.read(buffer)) > 0) {
            this.outputStream.write(buffer, 0, length);
        }

        // closing the steams
        stream.close();
        this.outputStream.closeEntry();
    }

    /**
     * Check if a specific entry has already been written
     *
     * @param entry     The entry to check
     * @return          true if the entry has been written,
     *                  false otherwise
     */
    public boolean hasEntry(String entry) {
        return this.entriesName.contains(entry);
    }

    /**
     * Close the streams
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {
        // return if the zipFile is null, aka if it wasn't created
        // why close streams that were not even created?
        if (this.zipFile == null) {
            return;
        }

        this.outputStream.close();
        this.zipOutputStream.close();
    }
}
