package fr.rader.imbob.utils.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipReader extends ZipFile {

    public ZipReader(File zip) throws IOException {
        super(zip);
    }

    public boolean hasEntry(String entryName) {
        return getEntry(entryName) != null;
    }

    public InputStream getEntryAsStream(String entryName) throws IOException {
        if (!hasEntry(entryName)) {
            return null;
        }

        return getInputStream(getEntry(entryName));
    }

    public void dumpToZipWriter(ZipWriter zipWriter) throws IOException {
        Iterator<? extends ZipEntry> entries = entries().asIterator();

        while (entries.hasNext()) {
            ZipEntry entry = entries.next();

            if (zipWriter.hasEntry(entry.getName())) {
                continue;
            }
            
            zipWriter.addEntry(
                    entry.getName(),
                    getInputStream(entry)
            );
        }
    }
}
