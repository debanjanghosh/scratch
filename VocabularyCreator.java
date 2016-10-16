package com.deft.sarcasm.preprocess;

//import com.config.ConfigConstants;
import com.deft.sarcasm.util.TextUtility;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrTokenizer;
//import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * @author Anusha Balakrishnan
 *         Date: 8/11/14
 *         Time: 5:36 AM
 */
public class VocabularyCreator {
    private TreeMap<String, Integer> unigrams;
    private TreeMap<String, Integer> bigrams;
    
//    private static Logger logger = Logger.getLogger(VocabularyCreator.class);
    private static  String PROPERTY_FILE  ;//ConfigConstants.PREPROCESS_PROPERTIES_FILE;
    private Properties properties;
    private static final String DEFAULT_NUMBER = "22" ;
    public VocabularyCreator()
    {
    	unigrams = new TreeMap<String, Integer>();
    	bigrams = new TreeMap<String, Integer>();
     
    /*    
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
        try {
            if(inputStream==null)
                throw new NullPointerException();
            properties.load(inputStream);
        }
        catch (IOException e) 
        {
       //     logger.warn("Unable to load properties file at "+PROPERTY_FILE+". Modify com.config.ConfigConstants to " +
        //            "modify the path or specify a new path for the properties file.");
        }
        catch (NullPointerException e) {
        //    logger.warn("Property file at "+ PROPERTY_FILE +" not found. Modify com.config.ConfigConstants to " +
         //           "modify existing path or specify a new path for the properties file.");
        }
       */ 
    }

    /**
     * Creates a vocabulary table that maps each word found in each file in the training directory to its frequency (total
     * number of occurrences across all files in the directory).
     * @param trainingDir The path of the directory in which all the training files are located
     * @param namePattern (String) Describes the pattern of the filenames for the training files to be read from. If not null,
     *                    vocabulary will only be extracted from files with filenames that match this pattern.
     *                    namePattern can be either a simple string or a regular expression.
     * @param write boolean parameter indicating whether the vocabulary should be written to the default vocabulary file
     *              or not. If true, the vocabulary created from the files in trainingDir is written to the vocabulary
     *              file specified by the path VOCABULARY_FILE in the properties file at
     *              ConfigConstants.PROCESS_PROPERTIES_FILE. If false, the vocabulary is written to standard output.
     * @throws IOException 
     * @throws InvalidFormatException 
     */
    public void createVocabulary(String trainingDir, String namePattern, boolean write, int tweet_column) throws InvalidFormatException, IOException
    {
    	
    	Set<String> uniques = new HashSet<String>() ;
        String OPENNLP_TOKENFILE = "en-token.bin";
        FileInputStream nlpStream = null;
      
        nlpStream = new FileInputStream("./data/config/" + OPENNLP_TOKENFILE );
        TokenizerModel model = new TokenizerModel(nlpStream);
        TokenizerME tokenizer = new TokenizerME(model);
        File dir = new File(trainingDir);
        if(!dir.isDirectory()) 
        {
            System.out.println("First parameter to createVocabulary(String, String) should be the path to a directory, not" +
                    " a file. Please check parameter and try again.\n\tProvided path: " + trainingDir);
            return;
        }
        TrainingFileFilter filter = new TrainingFileFilter(namePattern);
        for(File current: dir.listFiles(filter))
        {
        	System.out.println("Updating vocabulary using tokens in "+current.getName()) ;
        	List<String> lines = Files.readAllLines(Paths.get(current.toString()), StandardCharsets.UTF_8) ;
            for(String line : lines)
            {
                String[] fields = line.split("\t");
                String tweet;
                try
                {
                    tweet = fields[tweet_column];
                }
                catch (ArrayIndexOutOfBoundsException ai)
                {
                   tweet = line.trim();
                }
                    
                if(uniques.contains(tweet))
                {
        			continue ;
        		}
        		uniques.add(tweet) ;
        		
        //		if ( tweet.contains(":("))
        //		{
        //			System.out.println("here");
        //		}
                String[] tokens = tokenizer.tokenize(tweet);
                List<String> tokenList = preprocess(tokens) ;
                    
                for(int i = 0 ; i < tokenList.size() ; i++)
                {
                	String token = tokenList.get(i);
                	Integer old = unigrams.get(token) ;
                    if ( null == old )
                    {
                    	old = 0 ;
                    }
                    unigrams.put(token, 1+old);
                        	
                    if ( i < tokenList.size()-1)
                    {
                        String next_token = tokenList.get(i+1) ;
                        String bigram = token + "|||" + next_token ;
                        old = bigrams.get(bigram) ;
                        if ( null == old )
                        {
                            old = 0 ;
                        }
                        bigrams.put(bigram, 1+old);
                        
                    }
                }
         
            } 
        }
/*
        if(write) 
        {
          //  String vocabFile = getProperty("VOCABULARY_FILE");
           
            
            try {
                writeVocabulary(unigramFile,bigramFile);
        //        logger.info("Wrote tokens and frequencies from vocabulary to vocabulary file at "+vocabFile+". To change" +
                     //   " the write path, change the VOCABULARY_FILE property as needed in preprocess.properties.");
            } catch (FileNotFoundException e) {
          //      logger.warn("Could not find directory for vocabulary file at " + vocabFile);
            } catch (IOException e) {
            //    logger.warn("Error when writing to vocabulary file ("+vocabFile+")");
            }
        }
        else {
            displayVocabulary();
        }
 */       
    }
    
    private List<String> preprocess ( String[] tokens) throws IOException
    {
    	List<String> stopWordList = Files.readAllLines(Paths.get("./data/config/stopwords.txt"),
    			StandardCharsets.UTF_8);
    	
    	List<String> tokenList = new ArrayList<String>() ;
    	for ( int i = 0 ; i < tokens.length ;i++)
    	{
	    	String token = tokens[i] ;
	    	//escape anything XML
	        token = StringEscapeUtils.escapeXml(token).trim();
	        //if it is a number - convert to a default number
	        
	        //if all characters are uppercase - retain that, otherwise...
	        boolean ret = TextUtility.checkUppercase(token);
	        if (!ret)
	        {
	        	token = token.toLowerCase();
	        }
	        
	        //if any character is alpha-numeric, remove other than special chars
	        //and alpha-numeric
	        //otherwise it might be an emoticon/retain that
	        ret = TextUtility.isAlphaNumeric(token) ;
	        if (ret)
	        {
	        	 token = TextUtility.stripNonAlphaNumericAndSpecial(token) ;
	        }
	        else
	        {
	        	//do nothing - just check
	//            	System.out.println("here") ;
	        }
	        
	        ret  = TextUtility.CheckNumeric(token);
	        if (ret)
	        {
	        	token = DEFAULT_NUMBER ; // 22
	        }
	        
	        //remove any stop word
	        if (stopWordList.contains(token) )
	        {
	            continue ;
	        }
	        if ( token.startsWith("@"))
	        {
	        	token = "@user" ;
	        }
	        token = token.trim();
	        tokenList.add(token);
	        
    	}
    	
    	return tokenList ;
        
    }

    private void displayVocabulary()
    {
        for(String key: unigrams.keySet())
        {
            System.out.println(key+"\t"+unigrams.get(key));
        }
        
        for(String key: bigrams.keySet())
        {
            System.out.println(key+"\t"+bigrams.get(key));
        }
        
    }
    private void writeVocabulary(String unipath, String bipath) throws IOException 
    {
    	System.out.println("writing vocab in " + unipath) ;
        BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(
        	    new FileOutputStream(unipath), "UTF-8")) ;
        for(String key: unigrams.keySet())
        {
        	
        	if(key.equalsIgnoreCase(":)") )
        	{
        		System.out.println("here") ;
        	}
            writer.write(key+"\t"+unigrams.get(key));
            writer.newLine();
        }
        writer.close();
        
     	System.out.println("writing vocab in " + bipath) ;
        
        writer = new BufferedWriter (new OutputStreamWriter(
        	    new FileOutputStream(bipath), "UTF-8")) ;
        for(String key: bigrams.keySet())
        {
            writer.write(key+"\t"+bigrams.get(key));
            writer.newLine();
        }
        writer.close();
    }

    public String getProperty(String name)
    {
        return properties.getProperty(name);
    }

    public static void main(String[] args) throws IOException 
    {
    	String TRAINING_DATA_DIR = args[0] ;
    	String unigramFile = args[1];
    	String bigramFile = args[2] ;
        VocabularyCreator creator = new VocabularyCreator();
        int tweet_column = Integer.valueOf(args[3]) ;
       // creator.createVocabulary(TRAINING_DATA_DIR,vocabFile, "train_week_.*[^~]$", true,tweet_column);
        
        TRAINING_DATA_DIR = "./data/twitter_corpus/wsd/allData/" ;
        tweet_column = 1 ;
        creator.createVocabulary(TRAINING_DATA_DIR, "tweet.*ALLTARGETS.*.TRAIN", true,tweet_column);
        
        TRAINING_DATA_DIR = args[0] ;
        tweet_column = Integer.valueOf(args[3]) ;
  //      creator.createVocabulary(TRAINING_DATA_DIR, "SARCNOSARC.CONTEXT.TRAIN", true,tweet_column);

   //     creator.createVocabulary(TRAINING_DATA_DIR,vocabFile, "whiteblack.filtered.train", true,tweet_column);
        unigramFile = unigramFile + ".old" ;
        bigramFile = bigramFile + ".old" ;
        creator.writeVocabulary(unigramFile, bigramFile);
    }
}

/**
 * A class that implements FilenameFilter and only accepts files whose names match a specified pattern.
 */
class TrainingFileFilter implements FilenameFilter {
//    private static final Logger logger = Logger.getLogger(TrainingFileFilter.class);
    private Pattern namePattern;
    private Matcher nameMatcher;
    public TrainingFileFilter(String pattern)
    {
        super();
        if(pattern==null)
            pattern = ".*";
        namePattern = Pattern.compile(pattern);
        nameMatcher = null;
    }

    /**
     * Checks whether the file specified can be accepted by this filter or not. Only files that match the filter's
     * pattern and are not directories can be accepted by the filter.
     * @param dir The directory the file belongs to
     * @param name The name of the file
     * @return true iff the filename specified matches the filter's pattern and the filename doesn't refer to a
     * directory
     */
    @Override
    public boolean accept(File dir, String name) 
    {

        if(nameMatcher==null)
            nameMatcher = namePattern.matcher(name);
        else
            nameMatcher.reset(name);
//        String absolutePath = dir+"/"+name;
//        File currentFile = new File(absolutePath);
        return nameMatcher.find();
    }

}