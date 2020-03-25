package worker;

import Cluster_managment.ServiceRegistry;
import Networking.ONRequestCallBack;
import Networking.WebClient;
import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import model.proto.SearchModel;
import org.apache.zookeeper.KeeperException;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CoordinatorNode implements ONRequestCallBack {

    private static final String ENDPOINT = "/search";
    private static final String BOOKS_DIRECTORY = "/home/ankit/Documents/Distributed_Systems/Distributed_Document_Search/resources/books";
    private final ServiceRegistry workersServiceRegistry;
    private final WebClient client;
    private final List<String> documents;

    public CoordinatorNode(ServiceRegistry workersServiceRegistry,
                           WebClient client){
        this.workersServiceRegistry = workersServiceRegistry ;
        this.client = client ;
        this.documents = readDocumentsLists() ;
//        System.out.println("List of documents returned are : "+this.documents);
    }

    public List<String> readDocumentsLists(){
        File documentsDirectory = new File(BOOKS_DIRECTORY);
//        if(documentsDirectory.isDirectory())
//            System.out.println("Given address is a directory!!");
//        else
//            System.out.println("Given address is not a directory!!");
//        System.out.println("Books directory address is : "+documentsDirectory);
//        System.out.println("Books directories in books directory are : \n"+documentsDirectory.listFiles());
        return Arrays.asList(documentsDirectory.list());
//                .stream()
//                .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
//                .collect(Collectors.toList());
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) throws KeeperException, InterruptedException {
    try {

            SearchModel.Request request = SearchModel.Request.parseFrom(requestPayload);
            SearchModel.Response response = CreateResponse(request);
            return response.toByteArray() ;
        } catch (com.google.protobuf.InvalidProtocolBufferException invalidProtocolBufferException) {
        System.out.println("[CoordinatorNode] Error is occurring here") ;
        invalidProtocolBufferException.printStackTrace();
        return SearchModel.Response.getDefaultInstance().toByteArray() ;
    }

    }

    public SearchModel.Response CreateResponse(SearchModel.Request request) throws KeeperException, InterruptedException {
        SearchModel.Response.Builder searchResponse = SearchModel.Response.newBuilder() ;

        System.out.println("\n\n[+]Requested Query is : "+request.getSearchQuery());
        List<String> searchTerms = TFIDF.parseWordsFromLines(request.getSearchQuery()) ;

        List<String> workers = workersServiceRegistry.GetAllServiceAddresses();

        if(workers.size() == 0){
            System.out.println("[-]No worker available!!!");
        }

        List<Task> tasks = createTasks(searchTerms, workers.size()) ;
        List<Result> result = sendTask(workers, tasks) ;
        List<SearchModel.Response.DocumentStats> sortedDocs = AggregateResults(result, searchTerms) ;
        searchResponse.addAllRelevantDocuments(sortedDocs) ;

        return searchResponse.build() ;
    }

    public List<SearchModel.Response.DocumentStats> AggregateResults(List<Result> results, List<String> searchTerms){

        Map<String, DocumentData> allDocumentResults = new HashMap<>();

        for( Result result : results)
            allDocumentResults.putAll(result.getDocumentOfDocumentData());

        Map<Double, List<String>> scoreToDoc = TFIDF.getDocumentScores(searchTerms, allDocumentResults) ;

        return sortDocByScore(scoreToDoc) ;
    }

    private List<SearchModel.Response.DocumentStats> sortDocByScore(Map<Double, List<String>> scoreToDoc) {

        List<SearchModel.Response.DocumentStats> sortedDocuments = new ArrayList<>() ;

        for(Map.Entry<Double, List<String>> scoreDocPair : scoreToDoc.entrySet()){
            Double score = scoreDocPair.getKey() ;

            for(String doc : scoreDocPair.getValue()){
                File documentPath = new File(doc) ;
                SearchModel.Response.DocumentStats documentStats = SearchModel.Response.DocumentStats.newBuilder()
                        .setScore(score)
                        .setDocumentName(doc)
                        .setDocumentSize(doc.length())
                        .build() ;

                sortedDocuments.add(documentStats) ;
            }

        }

        return sortedDocuments ;
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }

    public List<List<String>> splitDocumetList(int numberOfWorkers){

        int numberOfDocPerWorker = (this.documents.size() + numberOfWorkers - 1)/numberOfWorkers ;

        List<List<String>> splittedDocuments = new ArrayList<>() ;

        for(int i = 0 ; i < numberOfWorkers ; ++i)
        {
            int start, end ;
            start = numberOfDocPerWorker*i ;
            end = Math.min((start + numberOfDocPerWorker - 1), this.documents.size());

            if(end < start) break ;

            List<String> CurrentWorkerDocuments = new ArrayList<>(documents.subList(start, end)) ;
            splittedDocuments.add(CurrentWorkerDocuments) ;
        }

        return splittedDocuments ;
    }

    public List<Task> createTasks(List<String> SearchTerms, int numberOfWorker){
        List<List<String>> splittedDocuments = splitDocumetList(numberOfWorker) ;

        List<Task> tasks = new ArrayList<>() ;

        for(List<String> documentForWorker : splittedDocuments){
            Task task = new Task(SearchTerms, documentForWorker) ;
            tasks.add(task) ;
        }
        return tasks ;
    }

    public List<Result> sendTask(List<String> workers, List<Task> tasks){
        CompletableFuture<Result>[] futures = new CompletableFuture[workers.size()] ;

        for(int i = 0 ; i < workers.size() ; ++i){
            String worker = workers.get(i) ;
            Task task = tasks.get(i) ;
            byte[] payload = SerializationUtils.serializeObject(task) ;

            futures[i] = client.sendTask(worker, payload) ;
        }

        List<Result> results = new ArrayList<>() ;
        for(CompletableFuture<Result> future : futures)
        {
            try{
                Result result = future.get() ;
                results.add(result) ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return results ;
    }
}
