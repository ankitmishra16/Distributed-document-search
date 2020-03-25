package worker;

import model.DocumentData;

import java.util.*;

public class TFIDF {

    public static Double calculateTermFrequency(List<String> words, String term){
        Double count = Double.valueOf(0);
        for(String word : words)
        {
            if(word.equalsIgnoreCase(term)) count++ ;
        }
        System.out.println("Term frequency for term : "+term+" is : "+count);

        return count ;
    }

    public static Double calculateInverseDocumentFrequency(String term,
                                                           Map<String, DocumentData> documentResults){

        Double count = Double.valueOf(0), numberOfDocument = Double.valueOf(documentResults.keySet().size()) ;
        for(String document : documentResults.keySet() )
        {
            if(documentResults.get(document).documentData.get(term) > 0.0 ) {
                count++ ;
//            System.out.println("Found!!!") ;
            }
        }
//        System.out.println("Number of documents : "+numberOfDocument+", count : "+count);
        return numberOfDocument == Double.valueOf(0) ? 0 : Math.log10(numberOfDocument/count) ;
    }

    public static DocumentData createDocumentData( List<String> words, List<String> terms) {
        DocumentData documentData = new DocumentData() ;

        for(String term : terms){
            double frequency = calculateTermFrequency(words, term) ;
            documentData.addDocumentData(term, frequency);
        }
        return documentData ;
    }

    public static List<String> parseWordsFromDocument(List<String> lines){

        List<String> words = new ArrayList<>() ;
        for(String line : lines)
            words.addAll(parseWordsFromLines(line)) ;

        return words ;
    }

    public static List<String> parseWordsFromLines(String line){
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static Map<String, Double> getTermToInverseDocumentFrequencyMap(List<String> terms,
                                                                        Map<String, DocumentData> documentResults ){
        Map<String, Double> termToIDF = new HashMap<>() ;

        for( String term : terms)
        {
            Double idf = calculateInverseDocumentFrequency(term, documentResults) ;
            termToIDF.put(term, idf) ;
        }

        return termToIDF ;

    }

    public static Double calculateDocumentScore(List<String> terms,
                                         DocumentData documentData,
                                         Map<String, Double> termToIDFMap){
        Double score = Double.valueOf(0) ;
        for(String term : terms)
        {
            Double tf = documentData.getFrequency(term) ;
            Double idf = termToIDFMap.get(term) ;
            score += (tf*idf) ;
//            System.out.println("TF : "+tf+", IDF : "+idf+", score = "+ score);

        }
//        System.out.println("Score of document is "+score) ;
        return score ;
    }

    public static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDoc,
                                                                          Double score,
                                                                          String document){
        List<String> documentsWithCurrentScore = scoreToDoc.get(score) ;
        if(documentsWithCurrentScore == null){
            documentsWithCurrentScore = new ArrayList<>() ;
        }

        documentsWithCurrentScore.add(document) ;
        scoreToDoc.put(score, documentsWithCurrentScore) ;

    }

    public static Map<Double, List<String>> getDocumentScores( List<String> terms,
                                                               Map<String, DocumentData> documentResults){
        TreeMap<Double, List<String>> scoreToDoc = new TreeMap<>() ;

        Map<String, Double> IDFMap = getTermToInverseDocumentFrequencyMap(terms, documentResults) ;

        for( String document : documentResults.keySet()){

            Double score = calculateDocumentScore(terms, documentResults.get(document), IDFMap) ;
            addDocumentScoreToTreeMap(scoreToDoc, score, document);
        }

        return scoreToDoc.descendingMap() ;
    }

}
