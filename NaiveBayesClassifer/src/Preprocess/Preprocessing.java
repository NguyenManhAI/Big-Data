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

    public Preprocessing() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String documentText, Boolean UseStopWord)
    {
        List<String> lemmas = new LinkedList<>();

        Annotation document = new Annotation(documentText);

        this.pipeline.annotate(document);
        List<String> stopWords = getStopWords();

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {

            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {

                String lemma = token.get(LemmaAnnotation.class);
                if(UseStopWord){
                    if(!stopWords.contains(lemma) && !isContainPunctuation(lemma)){
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

    public static boolean isContainPunctuation(String a){
        List<String> punctuations = getPunctuation();
        boolean result = false;
        for(String punctuation: punctuations) {
            if (a.contains(punctuation)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("Starting Stanford Lemmatizer");
        String text = "How could you be seeing into my eyes like open doors? \n"+
                "You led me down into my core where I've became so numb \n"+
                "Without a soul my spirit's sleeping somewhere cold \n"+
                "Until you find it there and led it back home \n"+
                "You woke me up inside \n"+
                "Called my name and saved me from the dark \n"+
                "You have bidden my blood and it ran \n"+
                "Before I would become undone \n"+
                "You saved me from the nothing I've almost become \n"+
                "You were bringing me to life \n"+
                "Now that I knew what I'm without \n"+
                "You can've just left me \n"+
                "You breathed into me and made me real \n"+
                "Frozen inside without your touch \n"+
                "Without your love, darling \n"+
                "Only you are the life among the dead \n"+
                "I've been living a lie, there's nothing inside \n"+
                "You were Bringing me to Life.";
        Preprocessing slem = new Preprocessing();
        String text2 = "I got a wild hair for taffy and ordered this five pound bag. "+
                "The taffy was all very enjoyable with many flavors: watermelon, root beer, melon, peppermint, "+
                "grape, etc. My only complaint is there was a bit too much red/black licorice-flavored pieces "+
                "(just not my particular favorites). Between me, my kids, and my husband, this lasted only two weeks! "+
                "I would recommend this brand of taffy -- it was a delightful treat.";
        String text3 = "\"Product arrived labeled as Jumbo Salted Peanuts...the"+
                " peanuts were actually small sized unsalted. Not sure if this was an error or if the vendor intended" +
                " to represent the product as \"\"Jumbo\"\".\"\t1";
        String text4 = "If you are looking for the secret ingredient in Robitussin I believe I have found it."+
                "  I got this in addition to the Root Beer"+
                " Extract I ordered (which was good) and made some cherry soda.  The flavor is very medicinal.\t2";
        String text5 = "I 'm a doctor.This has been 35 year since I graduated!!!!";
        String text6 = "That is beautiful item";
        String text7 = "He love this product like me";
        String text8 = "She very hate you!";
        System.out.println(slem.lemmatize(text8));
    }

}