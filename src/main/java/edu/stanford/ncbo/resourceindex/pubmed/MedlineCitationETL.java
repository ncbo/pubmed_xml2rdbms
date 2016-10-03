package edu.stanford.ncbo.resourceindex.pubmed;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

public class MedlineCitationETL {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationETL.class);

    private MedlineCitationETLProperties defaultProps;

    public MedlineCitationETL() {
        defaultProps = MedlineCitationETLProperties.getInstance();
    }

    private boolean loadMedlineFiles() {
        // Sanity check for valid data directory
        String path = defaultProps.getProperty("dataDirectory");
        File file = new File(path);
        boolean valid = file.exists() && file.isDirectory();
        if (!valid) {
            logger.error("Invalid data directory: {}", path);
            return false;
        }
        logger.info("Medline data directory: {}\n", file.getAbsolutePath());

        MedlineCitationHandler handler = new MedlineCitationHandler();
        MedlineCitationTranslator translator = new MedlineCitationTranslator();

        // TODO: If any SQLExceptions are thrown by instantiating a translator, don't continue.

        handler.addNewMedlineCitationListener(translator);

        // Load all Medline XML files in data directory.
        Collection<File> files = FileUtils.listFiles(file, new String[]{"xml.zip"}, false);

        /**
         * Sort the distribution files in reverse order by name so that the newest Medline data is loaded
         * into the database first.  Unfortunate hack to avoid making changes in the NCBO Resource Index project,
         * which currently has no easy means of ordering data before beginning it's ElasticSearch population.
         */
        File[] fileArray = files.stream().map(File::getAbsoluteFile).toArray(File[]::new);
        Arrays.sort(fileArray, NameFileComparator.NAME_INSENSITIVE_REVERSE);

        Boolean performValidation = Boolean.parseBoolean(defaultProps.getProperty("performValidation"));

        for (File f : fileArray) {
            String fileName = f.getName();
            logger.info("Load Medline file: {}", fileName);

            Instant start = Instant.now();

            MedlineCitationExtractor mce = new MedlineCitationExtractor(f, performValidation);
            InputStream inputStream = mce.extract();

            if (inputStream != null) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                try {
                    SAXParser parser = factory.newSAXParser();
                    parser.parse(inputStream, handler);
                    inputStream.close();

                    logger.info("Finished loading Medline file: {}", fileName);

                    Instant end = Instant.now();
                    Duration duration = Duration.between(start, end);
                    String formattedDuration = DurationFormatUtils.formatDurationHMS(duration.toMillis());
                    logger.info("Total load time for Medline file {}: {}\n", fileName, formattedDuration);

                } catch (IOException | ParserConfigurationException | SAXException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        handler.removeNewMedlineCitationListener(translator);

        return true;
    }

    public static void main(String[] args) {
        MedlineCitationETL loader = new MedlineCitationETL();

        logger.info("Begin loading 2016 MEDLINE/PubMed baseline database distribution");

        Instant start = Instant.now();
        loader.loadMedlineFiles();
        Instant end = Instant.now();

        logger.info("Finished loading 2016 MEDLINE/PubMed baseline database distribution");

        Duration duration = Duration.between(start, end);
        String formattedDuration = DurationFormatUtils.formatDurationHMS(duration.toMillis());
        logger.info("Total load time for 2016 MEDLINE/PubMed baseline database distribution: {}", formattedDuration);
    }

}
