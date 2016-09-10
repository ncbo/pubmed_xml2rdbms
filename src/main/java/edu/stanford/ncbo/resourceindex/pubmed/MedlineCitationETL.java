package edu.stanford.ncbo.resourceindex.pubmed;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class MedlineCitationETL {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationETL.class);

    private Properties defaultProps = new Properties();

    public MedlineCitationETL() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");

        try {
            defaultProps.load(in);
            in.close();
        } catch (IOException e) {
            logger.error("Failed to load properties file: {}", e.getMessage());
            e.printStackTrace();
        }
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
        logger.info("Medline data directory: {}", file.getAbsolutePath());

        MedlineCitationHandler handler = new MedlineCitationHandler();
        MedlineCitationTranslator translator = new MedlineCitationTranslator();
        handler.addNewMedlineCitationListener(translator);

        // Load all Medline XML files in data directory.
        Collection<File> files = FileUtils.listFiles(file, new String[]{"xml.zip"}, false);
        for (File f : files) {
            logger.info("Load Medline file: {}", f.getName());

            MedlineCitationExtractor mce = new MedlineCitationExtractor(f);
            InputStream inputStream = mce.extract();

            if (inputStream != null) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = null;
                try {
                    parser = factory.newSAXParser();
                    parser.parse(inputStream, handler);
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
        loader.loadMedlineFiles();
        logger.info("Finished loading 2016 MEDLINE/PubMed baseline database distribution");
    }

}
