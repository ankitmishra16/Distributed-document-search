package model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DocumentData implements Serializable {

    public Map<String, Double> documentData = new HashMap<>();

    public Map<String, Double> getDocumentData() {
        return Collections.unmodifiableMap(this.documentData);
    }

    public void addDocumentData(String term, Double frequency) {
        this.documentData.put(term, frequency);
    }

    public Double getFrequency(String term) { return this.documentData.get(term) ;}



}
