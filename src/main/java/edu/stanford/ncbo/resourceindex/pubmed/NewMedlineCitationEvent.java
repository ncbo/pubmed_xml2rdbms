package edu.stanford.ncbo.resourceindex.pubmed;

import java.util.EventObject;

public class NewMedlineCitationEvent extends EventObject {

    private MedlineCitation citation;

    public NewMedlineCitationEvent(Object source, MedlineCitation citation) {
        super(source);
        this.citation = citation;
    }

    public MedlineCitation getMedlineCitation() {
        return citation;
    }
}
