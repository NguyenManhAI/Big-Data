package Preprocess;/*
Tiền xử lý: Tokenization, Lower case, Lemmatization, Remove StopWords
*/
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class Preprocessing {

    protected StanfordCoreNLP pipeline;
    protected List<String> listStopWords;
    protected List<String> listPunctuations;

    public Preprocessing() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);
        this.listStopWords = getStopWords();
        this.listPunctuations = getPunctuation();
    }

    public List<String> lemmatize(String documentText, Boolean UseStopWord)
    {
        List<String> lemmas = new LinkedList<>();

        Annotation document = new Annotation(documentText);

        this.pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {

            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {

                String lemma = token.get(LemmaAnnotation.class);
                if(UseStopWord){

                    if(!this.listStopWords.contains(lemma) && !isContainPunctuation(lemma)){
                        lemmas.add(lemma);
                    }

                }else{
                    if(!isContainPunctuation(lemma)){
                        lemmas.add(lemma);
                    }
                }

            }
        }

        return lemmas;
    }

    public List<String> lemmatize(String documentText){
        Boolean UseStopWord = true;
        return lemmatize(documentText, UseStopWord);
    }

    public static List<String> getStopWords(){
        InputStream inputStream = Preprocessing.class.getResourceAsStream("/stopwords.txt");

        List<String> stopWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stopWords;
    }
    public static List<String> getPunctuation(){
        InputStream inputStream = Preprocessing.class.getResourceAsStream("/punctuation.txt");

        List<String> punctuations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                punctuations.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return punctuations;
    }

    public boolean isContainPunctuation(String a){
        boolean result = false;
        for(String punctuation: this.listPunctuations) {
            if (a.contains(punctuation)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String text1 = "I purchased it as I have been informed by my physician<br />to reduce my intake of salt";
        String text5 = "I 'm a doctor. This has been 35 year since I graduated!!!!";
        String text2 = "when added to milk,this leaves a bad aftertaste in the mouth. vanilla essence has a much cleaner and stronger Vendela flavour than this syrup";
        String text3 = "very good";

        Preprocessing slem = new Preprocessing();
        System.out.println(slem.lemmatize(text5));
        System.out.println(slem.lemmatize(text2));
    }
}