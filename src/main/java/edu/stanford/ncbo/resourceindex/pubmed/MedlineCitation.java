package edu.stanford.ncbo.resourceindex.pubmed;

import java.util.ArrayList;
import java.util.List;

public class MedlineCitation {

    private String pubMedId = "";

    private String articleTitle = "";

    private String abstractText = "";

    private List<String> keywords;

    private List<String> meshHeadings;

    public MedlineCitation() {
        keywords = new ArrayList<String>();
        meshHeadings = new ArrayList<String>();
    }

    public String getPubMedId() {
        return pubMedId;
    }

    public void setPubMedId(String pubMedId) {
        this.pubMedId = pubMedId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getMeshHeadings() {
        return meshHeadings;
    }

    public void setMeshHeadings(List<String> meshHeadings) {
        this.meshHeadings = meshHeadings;
    }

    @Override
    public String toString() {
        return String.format("MedlineCitation{ PubMed ID: %s, Article Title: %s }", this.pubMedId, this.articleTitle);
    }
}
