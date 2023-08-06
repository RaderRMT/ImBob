package fr.rader.imbob.utils.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.rader.imbob.windows.impl.LoggerWindow;

public class ZipWriter implements AutoCloseable {

    private static final String TEMP_ZIP_PREFIX = "imbob-zip";

    private static final int BUFFER_SIZE = 8192;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    private final File tempZip;
    private final File destination;

    private final FileOutputStream tempZipOutputStream;
    private final ZipOutputStream zipOutputStream;

    private final List<String> entries;

    private boolean isCreatingEntry = false;

    public ZipWriter(File destination) throws IOException {
        this.destination = destination;
        this.tempZip = File.createTempFile(TEMP_ZIP_PREFIX, null);
        this.entries = new ArrayList<>();

        this.tempZipOutputStream = new FileOutputStream(this.tempZip);
        this.zipOutputStream = new ZipOutputStream(this.tempZipOutputStream);
    }

    public OutputStream createEntry(String entryName) throws IOException {
        this.zipOutputStream.putNextEntry(new ZipEntry(entryName));
        this.entries.add(entryName);

        this.isCreatingEntry = true;

        return this.zipOutputStream;
    }

    public void closeEntry() throws IOException {
        if (!this.isCreatingEntry) {
            LoggerWindow.error("Cannot close Entry if not creating Entry");
            return;
        }

        this.isCreatingEntry = false;
        this.zipOutputStream.closeEntry();
    }

    public void addEntry(String entryName, InputStream data) throws IOException {
        if (this.isCreatingEntry) {
            LoggerWindow.error("Cannot add entry when creating Entry");
            return;
        }

        this.entries.add(entryName);
        this.zipOutputStream.putNextEntry(new ZipEntry(entryName));

        int dataLengthToWrite;
        while ((dataLengthToWrite = data.read(this.buffer)) > 0) {
            this.zipOutputStream.write(this.buffer, 0, dataLengthToWrite);
        }

        data.close();
        this.zipOutputStream.closeEntry();
    }

    @Override
    public void close() throws IOException {
        if (this.isCreatingEntry) {
            closeEntry();
        }

        this.zipOutputStream.close();
        this.tempZipOutputStream.close();
    }

    public void move() throws IOException {
        Files.move(
                this.tempZip.toPath(),
                this.destination.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    public boolean hasEntry(String entryName) {
        return this.entries.contains(entryName);
    }
}
