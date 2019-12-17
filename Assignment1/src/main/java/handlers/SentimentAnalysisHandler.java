package handlers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SentimentAnalysisHandler {

    private StanfordCoreNLP sentimentPipeline;
    private StanfordCoreNLP NERPipeline;

    public SentimentAnalysisHandler(){
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, parse, sentiment");
        this.sentimentPipeline = new StanfordCoreNLP(props);

        props = new Properties();
        props.put("annotators", "tokenize , ssplit, pos, lemma, ner");
        this.NERPipeline = new StanfordCoreNLP(props);
    }

    /**
     * Sentiment Analysis: Given a text find out its sentiment;
     * whether what the text is saying is positive/negative/neutral.
     * The tool gives a score between 0 = very negative up to 4 = very positive.
     */
    public int findSentiment(String review) {
        int mainSentiment = 0;
        if (review!= null && review.length() > 0) {
            int longest = 0;
            Annotation annotation = this.sentimentPipeline.process(review);
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        return mainSentiment;
    }

    /**
    Named Entity Extraction:
    Given a text extracts the entities of the text together with their entity type (e.g. Obama:Person)
    */
    public List<String> getListOfEntities(String review){
        List<String> entities = new ArrayList<>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(review);

        // run all Annotators on this text
        this.NERPipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                entities.add(word + ":" + ne);
            }
        }

        return entities;
    }
}