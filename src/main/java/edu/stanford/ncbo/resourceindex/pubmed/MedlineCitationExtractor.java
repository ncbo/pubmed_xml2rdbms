package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MedlineCitationExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationExtractor.class);

    private File file;

    public MedlineCitationExtractor(File file) {
        this.file = file;
    }

    public InputStream extract() {
        InputStream inputStream = null;

        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            // Per the National Library of Medicine's documentation, there's only one XML data file
            // inside of every ZIP file.
            if (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                inputStream = zipFile.getInputStream(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }

}
