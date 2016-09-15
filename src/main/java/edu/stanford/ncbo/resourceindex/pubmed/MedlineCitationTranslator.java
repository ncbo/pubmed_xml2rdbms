package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MedlineCitationTranslator implements NewMedlineCitationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationTranslator.class);

    private MedlineCitationETLProperties defaultProps;

    private Connection connection;

    public MedlineCitationTranslator() {
        defaultProps = MedlineCitationETLProperties.getInstance();
        getConnection();
    }

    private void getConnection() {
        try {
            String user = defaultProps.getProperty("jdbc.username");
            String password = defaultProps.getProperty("jdbc.password");
            String url = defaultProps.getProperty("jdbc.url");
            Connection connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logger.error("Can't connect to database: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleNewMedlineCitation(NewMedlineCitationEvent event) {
        MedlineCitation citation = event.getMedlineCitation();
        //logger.info("Write citation with ID: {} to database", citation.getPubMedId());
    }
}
