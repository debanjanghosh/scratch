package com.research.course.debate.LDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.stats.Statistics;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToDoubleMap;
import com.research.course.debate.util.DictionaryReaderFactoryBean;
import com.research.course.debate.util.DocumentProperty;
import com.research.course.debate.util.KrovetzStemmer;
import com.research.course.debate.util.NonAlphaStopTokenizerFactory;
import com.research.course.debate.util.PropertyType;
import com.research.course.debate.util.StemTokenizerFactory;

public class LDAGenerator 
{
    public static final int MINIMUM_TOKEN_COUNT = 2;
    private LatentDirichletAllocation ldaEngine;
    private SymbolTable ldaSymbolTable;
	private String outputDirectory;
    //    private TokenizerFactory textTokenizerFactory;
    public static final long RANDOM_SEED = 6474835;
    public static final int NUMSAMPLES = 3;
    public static final int BURNIN = 2;
    public static final int SAMPLELAG = 1;
    public static final int LDA_TOPIC_THRESH = 25;
    
    //debanjan
    public static final int LDA_TOPIC_MIN_THRESH = 15 ;
	private static final int Set = 0;
 
	public void setOutputPath ( String output )
	{
		this.outputDirectory = output ;
	}
	
	public void setLDaParameters( String extn) throws IOException, ClassNotFoundException
	{
		File stFile = new File(outputDirectory, "lda_symbols." + extn + ".bin");
    	SymbolTable symbolTable = (SymbolTable) AbstractExternalizable.readObject(stFile);
		
    	File modelFile = new File(outputDirectory, "lda_model." + extn + ".bin");
    	
    	LatentDirichletAllocation ldaEngine = (LatentDirichletAllocation) 
    			AbstractExternalizable.readObject(modelFile) ;
    	
    	setLdaSymbolTable(symbolTable);
    	setLdaEngine(ldaEngine) ;
   	
	}
	
    /**
     * Generate LDA metadata for the given document. Only the document body text
     * is used in this process.
     * <p>
     * A single property is created whose value is a delimited list of
     * topic and score pairs. E.g. "110=0.045|234=0.013|15=0.0015"
     * <p>
     * <p>
     * The topics are sorted from highest to lowest scores.
     * </p>
     * <p>
     * The property type is {@link PropertyType#LDA}
     * </p>
     * This method may return an empty list if there were less than
     * {@value #MINIMUM_TOKEN_COUNT} word tokens found in the document (see
     * {@link #MINIMUM_TOKEN_COUNT}) .
     * </p>
     *
     * @param document the document to generate LDA data for
     *
     * @return the list with a single property containing the LDA information or an
     *         empty list if no LDA topics were generated.
     * @throws Exception 
     *
     * @see LDADataUtil
     */
	
	
	
    protected DocumentProperty doGenerateMetadata(String document) throws Exception
    {
    	
    	//serialization - deserialization
    
        DocumentProperty ldaProperties = new DocumentProperty();
        String body = document ;
        String cleanBodyText =
                LdaNormalizer.cleanUpBodyText(LdaNormalizer.unEscapeHTML(body),
                                              new ArrayList<String>());

        SortedSet<LDADataPair> ldaMap = calculateLda(cleanBodyText);
        if (!ldaMap.isEmpty())
        {
            String ldaString = LDADataUtil.toString(ldaMap);
            ldaProperties = new DocumentProperty(ldaString, DocumentProperty.UNSPECIFIED_SCORE, PropertyType.LDA);
        }
        return ldaProperties;
    }

    @SuppressWarnings("unchecked")
    public SortedSet<LDADataPair> calculateLda(String body) throws Exception
    {
        TokenizerFactory textTokenizerFactory = createTokenizer();

        int[] tokenIds =
                LatentDirichletAllocation.tokenizeDocument(body,
                                                           textTokenizerFactory, ldaSymbolTable);
        SortedSet<LDADataPair> ldaData = new TreeSet<LDADataPair>();

        if (tokenIds.length >= MINIMUM_TOKEN_COUNT)
        {
            Random random = new Random(RANDOM_SEED);

            double[] topicDist =
                    ldaEngine.bayesTopicEstimate(tokenIds, NUMSAMPLES, BURNIN,
                                                 SAMPLELAG, random);

            ObjectToDoubleMap<Integer> topicSorter =
                    new ObjectToDoubleMap<Integer>();
            for (int tp = 0; tp < topicDist.length; ++tp)
            {
                topicSorter.set(tp, topicDist[tp]);
            }

            List<Integer> orderedTopics = topicSorter.keysOrderedByValueList();
            
            int min = Math.min(LDA_TOPIC_MIN_THRESH, orderedTopics.size());
            for ( int rank = 0 ; rank < min ; rank++)
            
          //  for (int rank = 0; rank < orderedTopics.size()
           //                    && rank < (2 * LDA_TOPIC_THRESH); ++rank)
            {
                int tp = orderedTopics.get(rank);
                double topicScore = topicDist[tp];
                float score = (float) (Math.round(topicScore * 10000) / 10000.0);
                if (score > 0)
                {
                    ldaData.add(new LDADataPair(tp, score));
                }
            }
        }
        
        
        
        return ldaData;
    }

    private TokenizerFactory createTokenizer() throws Exception
    {
    	java.util.Set<String> stops = loadStopWords();
        //TS
        TokenizerFactory factory = new RegExTokenizerFactory("[\\x2Da-zA-Z0-9]+");

        //TS
        factory = new NonAlphaStopTokenizerFactory(factory);

        //TS
        factory = new LowerCaseTokenizerFactory(factory);

        // TS
        factory = new EnglishStopTokenizerFactory(factory);

        // TS
        factory = new StopTokenizerFactory(factory,stops);
        
   	 // TS iff KStemmer is new
   		List<File> headwordFiles = listHeadwordFiles() ; 
   		List<File> conflationsFiles = listConflationsFiles();
   		DictionaryReaderFactoryBean dictBean = new DictionaryReaderFactoryBean(headwordFiles,conflationsFiles) ;
   		
   		
        KrovetzStemmer stemmer = new KrovetzStemmer();
        stemmer.setDictionary(dictBean.getDictionary());
        factory = new StemTokenizerFactory(factory, stemmer);
           
   		return factory;

    }

	private static List<File> listConflationsFiles() {
		// TODO Auto-generated method stub
		List<File> conflationsFiles = new ArrayList<File>();
		conflationsFiles.add(new File ( "./data/config/kstem/country_nationality.txt")) ;
		conflationsFiles.add(new File ( "./data/config/kstem/direct_conflations.txt")) ;
		conflationsFiles.add(new File ( "./data/config/kstem/tlr_conflations.txt")) ;
		conflationsFiles.add(new File ( "./data/config/kstem/novus_conflations.txt")) ;
		return conflationsFiles;
	}

	private static List<File> listHeadwordFiles() 
	{
		// TODO Auto-generated method stub
		List<File> headwordFiles = new ArrayList<File>();
		headwordFiles.add(new File ( "./data/config/kstem/dict_supplement.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/head_word_list.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/exception_words.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/proper_nouns.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/tlr_dict_supplement.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/novus_dict_supplement.txt")) ;
		
		return headwordFiles;
	}

    
    private java.util.Set<String> loadStopWords() throws IOException 
    {
		// TODO Auto-generated method stub
		String stopFile = "./config/lda/stopWordsDebate.lst" ;
		
		Set<String> stops = new HashSet<String>() ;
		
    	BufferedReader reader = new BufferedReader ( new FileReader ( stopFile)) ;
    	
    	while ( true )
    	{
    		String line = reader.readLine();
    		if ( null == line )
    		{
    			break ;
    		}
    		stops.add(line.trim());
    		
    	}
    	reader.close();
		return stops;
	}

	public int[] tokenizeDocument(String text ) throws Exception
    {
        TokenizerFactory tokenizer = createTokenizer();
        return LatentDirichletAllocation.tokenizeDocument(text, tokenizer, ldaSymbolTable);
    }


    /**
     * Generate LDA metadata for the given List of documents. Only the document body text
     * is used in this process.
     * <p>
     * A single property is created for each document whose value is a delimited list of
     * topic and score pairs. E.g. "110=0.045|234=0.013|15=0.0015"
     * <p>
     * <p>
     * The topics are sorted from highest to lowest scores.
     * </p>
     * <p>
     * The property type is {@link PropertyType#LDA}
     * </p>
     * <p>
     * This method may return an Map where each document has an empty list if there were less than
     * {@value #MINIMUM_TOKEN_COUNT} word tokens found in the document (see
     * {@link #MINIMUM_TOKEN_COUNT}) .
     * </p>
     *
     * @param documents
     *
     * @return Map<String, List<Documentproperty>> results
     */
/*
    @Override
    protected Map<String, List<DocumentProperty>> doGenerateMetadata(List<IRecDocument> documents)
    {
        Map<String, List<DocumentProperty>> results =
                new HashMap<String, List<DocumentProperty>>();

        for (IRecDocument document : documents)
        {
            List<DocumentProperty> ldaProperties = doGenerateMetadata(document);
            results.put(document.getDocumentId(), ldaProperties);
        }
        return results;
    }
*/
//    public TokenizerFactory getTextTokenizerFactory()
//    {
//        return textTokenizerFactory;
//    }
//
//    public void setTextTokenizerFactory(TokenizerFactory textTokenizerFactory)
//    {
//        this.textTokenizerFactory = textTokenizerFactory;
//    }

    public LatentDirichletAllocation getLdaEngine()
    {
        return ldaEngine;
    }

    public void setLdaEngine(LatentDirichletAllocation ldaEngine)
    {
        this.ldaEngine = ldaEngine;
    }

    public SymbolTable getLdaSymbolTable()
    {
        return ldaSymbolTable;
    }

    public void setLdaSymbolTable(SymbolTable ldaSymbolTable)
    {
        this.ldaSymbolTable = ldaSymbolTable;
    }
/*
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
*/	
}
