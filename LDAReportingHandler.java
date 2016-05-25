package com.research.course.debate.LDA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Strings;

public class LDAReportingHandler implements ObjectHandler<LatentDirichletAllocation.GibbsSample>
{

	private final SymbolTable mSymbolTable;
    private final long mStartTime;

    LDAReportingHandler(SymbolTable symbolTable)
    {
        mSymbolTable = symbolTable;
        mStartTime = System.currentTimeMillis();
    }

    public void handle(LatentDirichletAllocation.GibbsSample sample)
    {

        System.out.printf("Epoch=%3d   elapsed time=%s\n",
                          sample.epoch(),
                          Strings.msToString(System.currentTimeMillis() - mStartTime));

        if ((sample.epoch() % 10) == 0)
        {
            double corpusLog2Prob = sample.corpusLog2Probability();
            System.out.println("      log2 p(corpus|phi,theta)=" + corpusLog2Prob
                               + "     token cross-entropy rate=" + (-corpusLog2Prob / sample.numTokens()));
        }
    }

    void fullReport(LatentDirichletAllocation.GibbsSample sample,
                    int maxWordsPerTopic,
                    int maxTopicsPerDoc,
                    boolean reportTokens)
    {

        System.out.println("\nFull Report");

        int numTopics = sample.numTopics();
        int numWords = sample.numWords();
        int numDocs = sample.numDocuments();
        int numTokens = sample.numTokens();

        System.out.println("epoch=" + sample.epoch());
        System.out.println("numDocs=" + numDocs);
        System.out.println("numTokens=" + numTokens);
        System.out.println("numWords=" + numWords);
        System.out.println("numTopics=" + numTopics);

        for (int topic = 0; topic < numTopics; ++topic)
        {
            int topicCount = sample.topicCount(topic);
            ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
            for (int word = 0; word < numWords; ++word)
            {
                counter.set(Integer.valueOf(word), sample.topicWordCount(topic, word));
            }
            List<Integer> topWords = counter.keysOrderedByCountList();
            System.out.println("\nTOPIC " + topic + "  (total count=" + topicCount + ")");
            System.out.println("SYMBOL             WORD    COUNT   PROB          Z");
            System.out.println("--------------------------------------------------");
            for (int rank = 0; rank < maxWordsPerTopic && rank < topWords.size(); ++rank)
            {
                int wordId = topWords.get(rank);
                String word = mSymbolTable.idToSymbol(wordId);
                int wordCount = sample.wordCount(wordId);
                int topicWordCount = sample.topicWordCount(topic, wordId);
                double topicWordProb = sample.topicWordProb(topic, wordId);
                double z = binomialZ(topicWordCount,
                                     topicCount,
                                     wordCount,
                                     numTokens);

                System.out.printf("%6d  %15s  %7d   %4.3f  %8.1f\n",
                                  wordId,
                                  word,
                                  topicWordCount,
                                  topicWordProb,
                                  z);
            }
        }

        for (int doc = 0; doc < numDocs; ++doc)
        {
            int docCount = 0;
            for (int topic = 0; topic < numTopics; ++topic)
            { docCount += sample.documentTopicCount(doc, topic); }
            ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
            for (int topic = 0; topic < numTopics; ++topic)
            {
                counter.set(Integer.valueOf(topic), sample.documentTopicCount(doc, topic));
            }
            List<Integer> topTopics = counter.keysOrderedByCountList();
            System.out.println("\nDOC " + doc);
            System.out.println("TOPIC    COUNT    PROB");
            System.out.println("----------------------");
            for (int rank = 0; rank < topTopics.size() && rank < maxTopicsPerDoc; ++rank)
            {
                int topic = topTopics.get(rank);
                int docTopicCount = sample.documentTopicCount(doc, topic);
                double docTopicPrior = sample.documentTopicPrior();
                double docTopicProb = (sample.documentTopicCount(doc, topic) + docTopicPrior)
                                      / (docCount + numTopics * docTopicPrior);
                System.out.printf("%5d  %7d   %4.3f\n",
                                  topic,
                                  docTopicCount,
                                  docTopicProb);
            }
            System.out.println();
            if (!reportTokens) continue;
            int numDocTokens = sample.documentLength(doc);
            for (int tok = 0; tok < numDocTokens; ++tok)
            {
                int symbol = sample.word(doc, tok);
                short topic = sample.topicSample(doc, tok);
                String word = mSymbolTable.idToSymbol(symbol);
                System.out.print(word + "(" + topic + ") ");
            }
            System.out.println();
        }
    }

    public void reportTopicsByZ(LatentDirichletAllocation.GibbsSample sample, int maxWords, int minCount)
    {
        int numTokens = sample.numTokens();
        for (int topic = 0; topic < sample.numTopics(); ++topic)
        {
            int topicCount = sample.topicCount(topic);
            ObjectToDoubleMap<Integer> wordToZ = new ObjectToDoubleMap<Integer>();
            for (int word = 0; word < sample.numWords(); ++word)
            {
                int topicWordCount = sample.topicWordCount(topic, word);
                if (topicWordCount < minCount) continue;
                int wordCount = sample.wordCount(word);
                double z = binomialZ(topicWordCount, topicCount, wordCount, numTokens);
                wordToZ.set(word, z);
            }
            List<Integer> topWords = wordToZ.keysOrderedByValueList();
            System.out.println("\nTOPIC " + topic + "  (total count=" + topicCount + ")");
            System.out.println("SYMBOL             WORD    COUNT   PROB          Z");
            System.out.println("--------------------------------------------------");
            for (int rank = 0; rank < maxWords && rank < topWords.size(); ++rank)
            {
                int wordId = topWords.get(rank);
                String word = mSymbolTable.idToSymbol(wordId);
                int topicWordCount = sample.topicWordCount(topic, wordId);
                double topicWordProb = sample.topicWordProb(topic, wordId);
                double z = wordToZ.get(wordId);
                System.out.printf("%6d  %15s  %7d   %4.3f  %8.1f\n",
                                  wordId, word, topicWordCount, topicWordProb, z);
            }
        }
    }

    void writeReport(LatentDirichletAllocation.GibbsSample sample,
                     int maxWordsPerTopic,
                     int maxTopicsPerDoc,
                     File outputfile,
                     boolean reportTokens)
    {


        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter(outputfile);


            pw.println("\nFull Report");

            int numTopics = sample.numTopics();
            int numWords = sample.numWords();
            int numDocs = sample.numDocuments();
            int numTokens = sample.numTokens();

            pw.println("epoch=" + sample.epoch());
            pw.println("numDocs=" + numDocs);
            pw.println("numTokens=" + numTokens);
            pw.println("numWords=" + numWords);
            pw.println("numTopics=" + numTopics);

            for (int topic = 0; topic < numTopics; ++topic)
            {
                int topicCount = sample.topicCount(topic);
                ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
                for (int word = 0; word < numWords; ++word)
                { counter.set(Integer.valueOf(word), sample.topicWordCount(topic, word)); }
                List<Integer> topWords = counter.keysOrderedByCountList();
                pw.println("\nTOPIC " + topic + "  (total count=" + topicCount + ")");
                pw.println("SYMBOL             WORD    COUNT   PROB          Z");
                pw.println("--------------------------------------------------");
                for (int rank = 0; rank < maxWordsPerTopic && rank < topWords.size(); ++rank)
                {
                    int wordId = topWords.get(rank);
                    String word = mSymbolTable.idToSymbol(wordId);
                    int wordCount = sample.wordCount(wordId);
                    int topicWordCount = sample.topicWordCount(topic, wordId);
                    double topicWordProb = sample.topicWordProb(topic, wordId);
                    double z = binomialZ(topicWordCount,
                                         topicCount,
                                         wordCount,
                                         numTokens);

                    pw.printf("%6d  %15s  %7d   %4.3f  %8.1f\n",
                              wordId,
                              word,
                              topicWordCount,
                              topicWordProb,
                              z);
                }
            }

            for (int doc = 0; doc < numDocs; ++doc)
            {
                int docCount = 0;
                for (int topic = 0; topic < numTopics; ++topic)
                { docCount += sample.documentTopicCount(doc, topic); }
                ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
                for (int topic = 0; topic < numTopics; ++topic)
                { counter.set(Integer.valueOf(topic), sample.documentTopicCount(doc, topic)); }
                List<Integer> topTopics = counter.keysOrderedByCountList();
                pw.println("\nDOC " + doc);
                pw.println("TOPIC    COUNT    PROB");
                pw.println("----------------------");
                for (int rank = 0; rank < topTopics.size() && rank < maxTopicsPerDoc; ++rank)
                {
                    int topic = topTopics.get(rank);
                    int docTopicCount = sample.documentTopicCount(doc, topic);
                    double docTopicPrior = sample.documentTopicPrior();
                    double docTopicProb = (sample.documentTopicCount(doc, topic) + docTopicPrior)
                                          / (docCount + numTopics * docTopicPrior);
                    pw.printf("%5d  %7d   %4.3f\n",
                              topic,
                              docTopicCount,
                              docTopicProb);
                }
                pw.println();
                if (!reportTokens) continue;
                int numDocTokens = sample.documentLength(doc);
                for (int tok = 0; tok < numDocTokens; ++tok)
                {
                    int symbol = sample.word(doc, tok);
                    short topic = sample.topicSample(doc, tok);
                    String word = mSymbolTable.idToSymbol(symbol);
                    pw.print(word + "(" + topic + ") ");
                }
                pw.println();
            }
        }
        catch (FileNotFoundException ignored)
        {
            System.err.println("Cannot write to file " + outputfile);
        }
        finally
        {
            if (pw != null)
            {
                pw.close();
            }
        }
    }

    static double binomialZ(double wordCountInDoc, double wordsInDoc,
                            double wordCountinCorpus, double wordsInCorpus)
    {
        double pCorpus = wordCountinCorpus / wordsInCorpus;
        double var = wordsInCorpus * pCorpus * (1 - pCorpus);
        double dev = Math.sqrt(var);
        double expected = wordsInDoc * pCorpus;
        double z = (wordCountInDoc - expected) / dev;
        return z;
    }

}
