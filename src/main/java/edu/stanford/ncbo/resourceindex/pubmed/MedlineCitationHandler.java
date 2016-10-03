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

    private boolean setPMID = true;

    private List<NewMedlineCitationEventListener> newCitationListeners = new ArrayList<>();

    private List<MedlineDocumentEndEventListener> documentEndListeners = new ArrayList<>();

    private MedlineCitation citation;

    private StringBuilder characterBuffer;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "MedlineCitation":
                citation = new MedlineCitation();
                break;
            case "CommentsCorrectionsList":
                setPMID = false;
                break;
        }

        characterBuffer = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String characterValue = characterBuffer.toString();

        switch (qName) {
            case "PMID":
                if (setPMID) {
                    citation.setPubMedId(characterValue);
                }
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
            case "DescriptorName":
                citation.getMeshHeadings().add(characterValue);
                break;
            case "CommentsCorrectionsList":
                setPMID = true;
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

    @Override
    public void endDocument() throws SAXException {
        fireMedlineDocumentEndEvent();
    }

    public synchronized void addNewMedlineCitationListener(NewMedlineCitationEventListener listener) {
        newCitationListeners.add(listener);
    }

    public synchronized void removeNewMedlineCitationListener(NewMedlineCitationEventListener listener) {
        newCitationListeners.remove(listener);
    }

    private synchronized void fireNewMedlineCitationEvent() {
        NewMedlineCitationEvent event = new NewMedlineCitationEvent(this, citation);
        for (NewMedlineCitationEventListener listener : newCitationListeners) {
            listener.handleNewMedlineCitation(event);
        }
    }

    public synchronized void addMedlineDocumentEndListener(MedlineDocumentEndEventListener listener) {
        documentEndListeners.add(listener);
    }

    public synchronized void removeMedlineDocumentEndListener(MedlineDocumentEndEventListener listener) {
        documentEndListeners.remove(listener);
    }

    private synchronized void fireMedlineDocumentEndEvent() {
        MedlineDocumentEndEvent event = new MedlineDocumentEndEvent(this);
        for (MedlineDocumentEndEventListener documentEndListener : documentEndListeners) {
            documentEndListener.handleMedlineDocumentEndEvent(event);
        }
    }

}
