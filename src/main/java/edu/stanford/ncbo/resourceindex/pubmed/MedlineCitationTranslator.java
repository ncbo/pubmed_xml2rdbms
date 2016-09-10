package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedlineCitationTranslator implements NewMedlineCitationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationTranslator.class);

    @Override
    public void handleNewMedlineCitation(NewMedlineCitationEvent event) {
        MedlineCitation citation = event.getMedlineCitation();
        //logger.info("Write citation with ID: {} to database", citation.getPubMedId());
    }
}
