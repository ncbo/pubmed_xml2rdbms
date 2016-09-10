package edu.stanford.ncbo.resourceindex.pubmed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class MedlineCitationHandler extends DefaultHandler {

    private static final Logger logger = LoggerFactory.getLogger(MedlineCitationHandler.class);

    private List<NewMedlineCitationEventListener> listeners = new ArrayList<>();

    private MedlineCitation citation;

    private StringBuilder characterBuffer;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("MedlineCitation")) {
            citation = new MedlineCitation();
        }
        characterBuffer = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String characterValue = characterBuffer.toString();

        switch (qName) {
            case "PMID":
                citation.setPubMedId(Integer.parseInt(characterValue));
                break;
            case "ArticleTitle":
                citation.setArticleTitle(characterValue);
                break;
            case "AbstractText":
                citation.setAbstractText(characterValue);
                break;
            case "Keyword":
                citation.getKeywords().add(characterValue);
                break;
            case "MedlineCitation":
                fireNewMedlineCitationEvent();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        characterBuffer.append(ch, start, length);
    }

    public synchronized void addNewMedlineCitationListener(NewMedlineCitationEventListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeNewMedlineCitationListener(NewMedlineCitationEventListener listener) {
        listeners.remove(listener);
    }

    private synchronized void fireNewMedlineCitationEvent() {
        NewMedlineCitationEvent event = new NewMedlineCitationEvent(this, citation);
        for (NewMedlineCitationEventListener listener : listeners) {
            listener.handleNewMedlineCitation(event);
        }
    }

}
