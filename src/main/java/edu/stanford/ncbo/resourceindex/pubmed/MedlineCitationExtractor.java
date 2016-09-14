package edu.stanford.ncbo.resourceindex.pubmed;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class MedlineCitationExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationExtractor.class);

    private File file;

    public MedlineCitationExtractor(File file) {
        this.file = file;
    }

    public InputStream extract() {
        InputStream inputStream = null;

        if (!isValidDataFile()) return null;

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

    private boolean isValidDataFile() {
        String expectedChecksum = "";
        String actualChecksum = "";

        // Get accompanying md5 checksum file for data file
        Path path = Paths.get(file.getAbsolutePath() + ".md5");
        if (!Files.exists(path)) {
            logger.error("Nonexistent checksum file: {}", path.toString());
            return false;
        }

        /*
         * Read contents of md5 checksum file to get expected checksum
         * Example content: "MD5 (medline16n0001.xml.zip) = 62a862002227d8f1e476a7c47e8aca61"
         */
        try {
            String contents = new String(Files.readAllBytes(path));
            expectedChecksum = contents.split(" = ")[1].trim();
            logger.debug("Expected checksum for {}: {}", file.getName(), expectedChecksum.toLowerCase());
        } catch (IOException e) {
            logger.error("Bad checksum file {}: {}", path.toString(), e.getMessage());
            e.printStackTrace();
        }

        // Get md5 checksum of data file
        try {
            HashCode hashCode = com.google.common.io.Files.hash(file, Hashing.md5());
            byte[] bytes = hashCode.asBytes();
            actualChecksum = printHexBinary(bytes);
            logger.debug("Actual checksum for {}: {}", file.getName(), actualChecksum.toLowerCase());
        } catch (IOException e) {
            logger.error("Failed to get checksum for {}: {}", file.getAbsolutePath(), e.getMessage());
            e.printStackTrace();
        }

        return actualChecksum.equalsIgnoreCase(expectedChecksum);
    }

}
