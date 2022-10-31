package fr.rader.imbob.utils.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import fr.rader.imbob.windows.impl.LoggerWindow;

public class ZipReader {

    /** This is the ZIP file we're reading */
    private final File file;

    /** We will use this to read entries in the ZIP file.
     * It is required, so if we can't read the file, we'll throw errors */
    private ZipFile zipFile = null;

    /**
     * Open a ZIP file for reading
     *
     * @param file The ZIP file to read
    */
    public ZipReader(File file) {
        this.file = file;

        try {
            LoggerWindow.info("Reading ZIP file \"" + this.file.getName() + '"');
            // we try opening the zip file
            this.zipFile = new ZipFile(this.file);
        } catch (ZipException e) {
            LoggerWindow.error("ZIP format error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            LoggerWindow.error("I/O error: " + e.getLocalizedMessage());
        }
    }

    /**
     * Look if the ZIP file contains the give entry
     *
     * @param entryName The entry to look for
     * @return          True if the ZIP file contains the entry, false otherwise
    */
    public boolean hasEntry(String entryName) {
        // the zip file *can* be null. In practice,
        // this should never happen, but we'll check
        // for it anyway. if it is null, we return false
        // as nothing can't contain an entry
        if (this.zipFile == null) {
            LoggerWindow.error(this.file.getName() + " is null!");
            return false;
        }

        LoggerWindow.info("Looking for entry \"" + entryName + '"');
        // we try to get the entry from the zip.
        // the entry will be null if it doesn't exist,
        // so we just have to return true if the
        // entry is not null, and false otherwise.
        boolean hasEntry = this.zipFile.getEntry(entryName) != null;
        // we print nice messages depending on
        // if the entry has been found or not
        if (!hasEntry) {
            LoggerWindow.error(this.file.getName() + " does not contain entry \"" + entryName + '"');
        } else {
            LoggerWindow.info("Entry \"" + entryName + "\" exists");
        }

        // and finally we return the boolean
        return hasEntry;
    }

    /**
     * Get an entry from the ZIP file as an InputStream
     *
     * @param entryName    The entry to get
     * @return             The entry's input stream, null if the entry
     *                     does not exist or if the zip file is null
     * @throws IOException If an I/O error occurred
    */
    public InputStream getEntry(String entryName) {
        // the zip file *can* be null. In practice,
        // this should never happen, but we'll check
        // for it anyway. if it is null, we return null
        // as nothing can't contain an entry
        if (this.zipFile == null) {
            LoggerWindow.error(this.file.getName() + " is null!");
            return null;
        }

        // we check if the entry exist.
        // if it does not, we return null
        if (!hasEntry(entryName)) {
            return null;
        }

        // we initialize the stream to return to null,
        // so we can return null if an IO error occurs
        InputStream entryStream = null;
        try {
            // as we know the entry exist, we
            // get it from the ZIP file and we
            // get it as an input stream
            entryStream = this.zipFile.getInputStream(this.zipFile.getEntry(entryName));
        } catch (IOException e) {
            LoggerWindow.error("I/O error: " + e.getLocalizedMessage());
        }

        // we print an error message if we couldn't read the entry
        if (entryStream == null) {
            LoggerWindow.error("Entry \"" + entryName + "\" couldn't be read.");
        }

        // we return the entry, or null if we couldn't read it
        return entryStream;
    }

    /**
     * Copy all the entries in the ZipReader to a {@link ZipWriter}.
     * Only entries that are not in the ZipWriter will be copied over.
     *
     * @param writer    The zip writer to write to
     */
    public void dumpToZipWriter(ZipWriter writer) {
        // we get the zip entries as an iterator
        Iterator<? extends ZipEntry> zipIterator = this.zipFile.entries().asIterator();
        // then we iterate through the iterator
        while (zipIterator.hasNext()) {
            ZipEntry entry = zipIterator.next();

            // if the writer already has the entry, we skip it
            if (writer.hasEntry(entry.getName())) {
                continue;
            }

            try {
                // if the writer doesn't have the entry, we add it and go to the next
                writer.addEntry(entry.getName(), this.zipFile.getInputStream(entry));
            } catch (IOException e) {
                LoggerWindow.error("I/O error: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Close any open streams
     */
    public void close() {
        // the zip file *can* be null. In practice,
        // this should never happen, but we'll check
        // for it anyway. if it is null, we just return
        // as no streams are open
        if (this.zipFile == null) {
            return;
        }

        try {
            // we close the zip file's stream(s)
            this.zipFile.close();
        } catch (IOException e) {
            LoggerWindow.error("I/O error: " + e.getLocalizedMessage());
        }
    }
}
