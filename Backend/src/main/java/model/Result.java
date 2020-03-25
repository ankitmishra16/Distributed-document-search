package model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {

    public Map<String, DocumentData> documentOfDocumentData = new HashMap<>() ;

    public void addDocumentOfDocumentData( String document, DocumentData data){
        documentOfDocumentData.put(document, data) ;
    }

    public Map< String, DocumentData> getDocumentOfDocumentData(){
        return Collections.unmodifiableMap(this.documentOfDocumentData) ;
    }
}
