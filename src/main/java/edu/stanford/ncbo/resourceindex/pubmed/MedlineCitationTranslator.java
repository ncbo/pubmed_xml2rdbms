package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.sql.*;
import java.util.List;

public class MedlineCitationTranslator implements NewMedlineCitationEventListener, MedlineDocumentEndEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationTranslator.class);

    private Connection connection;

    private int batchSize = 1000;

    private int counter = 0;

    private MedlineCitationETLProperties props;

    private PreparedStatement preparedStatement;

    private String databaseName;

    private String tableName;

    public MedlineCitationTranslator() {
        props = MedlineCitationETLProperties.getInstance();
        databaseName = props.getProperty("jdbc.dbName");
        tableName = props.getProperty("jdbc.tableName");
        initConnection();
        initTable();
    }

    @Override
    public void handleNewMedlineCitation(NewMedlineCitationEvent event) {
        MedlineCitation citation = event.getMedlineCitation();

        try {
            String id = citation.getPubMedId();
            StringReader reader = new StringReader(id);
            preparedStatement.setCharacterStream(1, reader, id.length());

            preparedStatement.setString(2, citation.getArticleTitle());
            preparedStatement.setString(3, citation.getAbstractText());

            List keywordList = citation.getKeywords();
            String keywords = (keywordList.isEmpty()) ? "" : String.join(",", keywordList);
            preparedStatement.setString(4, keywords);

            List meshHeadingsList = citation.getMeshHeadings();
            String meshHeadings = (meshHeadingsList.isEmpty()) ? "" : String.join(",", meshHeadingsList);
            preparedStatement.setString(5, meshHeadings);

            preparedStatement.addBatch();
            counter++;

            if (counter % batchSize == 0) {
                int[] results = preparedStatement.executeBatch();
                connection.commit();
                logger.info("Committed a batch. Affected rows: {}", results.length);
            }

        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    @Override
    public void handleMedlineDocumentEndEvent(MedlineDocumentEndEvent event) {
        try {
            int[] results = preparedStatement.executeBatch();
            connection.commit();
            logger.info("Reached end of document. Flush remaining batches. Affected rows: {}", results.length);
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    private void initConnection() {
        String url = props.getProperty("jdbc.url");
        String user = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        /**
         * Using "ignore" in SQL because NLM's XML files distribution contains a few instances of Medline
         * citation objects with duplicate PubMed IDs (duplicates are differentiated by a VersionID attribute).
         * Current DB schema has a uniqueness constraint on the local_element_id column that houses the PubMed ID.
         * In this version of the code, we've chosen not to maintain multiple versions of Medline citations and/or
         * to add logic that updates content.
         */
        String insertString = String.format("INSERT IGNORE INTO %s.%s (local_element_id, pm_title, pm_abstract, " +
                "pm_keywords, pm_meshheadings) VALUES (?,?,?,?,?)", databaseName, tableName);

        try {
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(insertString);
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    private void initTable(){
        Boolean reinitialize = Boolean.parseBoolean(props.getProperty("reinitializeTable"));
        String createString =
                String.format("CREATE TABLE %s.%s (id integer(10) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                        "local_element_id varchar(255) NOT NULL, pm_title text, pm_abstract text, " +
                        "pm_keywords text, pm_meshheadings text, PRIMARY KEY (id), " +
                        "UNIQUE KEY local_element_id (local_element_id))", databaseName, tableName);
        String dropString = String.format("DROP TABLE IF EXISTS %s.%s", databaseName, tableName);

        try {
            Statement statement = connection.createStatement();
            if (reinitialize) {
                statement.executeUpdate(dropString);
            }
            statement.executeUpdate(createString);
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    private static void logSQLException(
            SQLException e) {
        if (e != null) {
            logger.error("SQLState: {}", ((SQLException)e).getSQLState());
            logger.error("Error Code: {}", ((SQLException)e).getErrorCode());
            logger.error("Message: {}", e.getMessage());
            e.printStackTrace();
        }
    }

}
