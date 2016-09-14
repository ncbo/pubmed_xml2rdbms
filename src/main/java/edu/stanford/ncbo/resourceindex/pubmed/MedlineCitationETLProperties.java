package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MedlineCitationETLProperties extends Properties {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationETLProperties.class);

    private static MedlineCitationETLProperties instance = null;

    private MedlineCitationETLProperties() {
    }

    public static MedlineCitationETLProperties getInstance() {
        if (instance == null) {
            try {
                instance = new MedlineCitationETLProperties();
                InputStream in =
                        MedlineCitationETLProperties.class.getClassLoader().getResourceAsStream("config.properties");
                instance.load(in);
                in.close();
            } catch (IOException e) {
                logger.error("Failed to load properties file: {}", e.getMessage());
                e.printStackTrace();
            }
        }

        return instance;
    }
}
