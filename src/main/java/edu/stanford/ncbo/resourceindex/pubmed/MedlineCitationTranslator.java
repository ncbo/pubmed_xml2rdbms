package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MedlineCitationTranslator implements NewMedlineCitationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationTranslator.class);

    private MedlineCitationETLProperties props;

    private Connection connection;

    public MedlineCitationTranslator() {
        props = MedlineCitationETLProperties.getInstance();
        getConnection();
        initializeTable();
    }

    @Override
    public void handleNewMedlineCitation(NewMedlineCitationEvent event) {
        MedlineCitation citation = event.getMedlineCitation();
        //logger.info("Write citation with ID: {} to database", citation.getPubMedId());
    }

    private void getConnection() {
        String url = props.getProperty("jdbc.url");
        String user = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    private void initializeTable(){
        String dbName = props.getProperty("jdbc.dbName");
        String tableName = props.getProperty("jdbc.tableName");
        Boolean reinitialize = Boolean.parseBoolean(props.getProperty("reinitializeTable"));

        String createString =
                String.format("CREATE TABLE %s.%s (id integer(10) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                        "local_element_id varchar(255) NOT NULL, pm_title text, pm_abstract text, " +
                        "pm_keywords text, pm_meshheadings text, PRIMARY KEY (id), " +
                        "UNIQUE KEY local_element_id (local_element_id))", dbName, tableName);

        try {
            Statement statement = connection.createStatement();
            if (reinitialize) {
                statement.executeUpdate(String.format("DROP TABLE IF EXISTS %s.%s", dbName, tableName));
            }
            statement.executeUpdate(createString);
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    private static void logSQLException(SQLException e) {
        if (e != null) {
            logger.error("SQLState: {}", ((SQLException)e).getSQLState());
            logger.error("Error Code: {}", ((SQLException)e).getErrorCode());
            logger.error("Message: {}", e.getMessage());
            e.printStackTrace();
        }
    }

}
