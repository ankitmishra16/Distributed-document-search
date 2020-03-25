package worker;

import Networking.ONRequestCallBack;
import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class worker implements ONRequestCallBack {

    private static final String BOOKS_DIRECTORY = "/home/ankit/Documents/Distributed_Systems/Distributed_Document_Search/resources/books";

    private static final String ENDPOINT = "/task";
    @Override
    public byte[] handleRequest(byte[] requestPayload) {

        Task task = (Task) SerializationUtils.deserialize(requestPayload) ;
        Result result = createResult(task) ;

        return SerializationUtils.serializeObject(result) ;
    }

    private Result createResult(Task task)
    {
        List<String> documents = task.getDocuments() ;
        Result result = new Result() ;
        for(String document : documents)
        {
            List<String> words = parseWordsFromDocument(document) ;
//            System.out.println("Number of words in "+document+" : "+words.size());
            DocumentData documentData = TFIDF.createDocumentData(words, task.getSearchTerms()) ;
            result.addDocumentOfDocumentData(document, documentData);
        }
        return result ;
    }

    private List<String> parseWordsFromDocument(String document)
    {
        try{
            FileReader fileReader = new FileReader(BOOKS_DIRECTORY+"/"+document) ;
            BufferedReader bufferedReader = new BufferedReader(fileReader) ;
            List<String> lines = bufferedReader.lines().collect(Collectors.toList()) ;
            System.out.println("Number of lines in "+document+" is "+lines.size());
            List<String> words = TFIDF.parseWordsFromDocument(lines) ;
            return words ;
        } catch (FileNotFoundException e) {
            System.out.println(BOOKS_DIRECTORY+"/"+document+" file not found");
            return Collections.emptyList() ;
        }
    }
    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }
}
