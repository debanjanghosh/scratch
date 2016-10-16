package com.deft.sarcasm.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.deft.sarcasm.features.EXPERIMENT_MODE;
import com.deft.sarcasm.train.SarcasmResourceLoader;
import com.deft.sarcasm.train.SarcasmTrainHandler.unigramTypeEnum;
import com.deft.sarcasm.util.TextUtility;

import edu.stanford.nlp.util.StringUtils;

import org.apache.commons.lang3.StringEscapeUtils;




public class BoWFeatureLoader extends FeatureLoader
{
//	private List<String> unigramList;

	private Map<String,Integer> unigramMap;
	
	
	private SarcasmResourceLoader sarcasmRLObject;

	private ArrayList<String> hashTagList;

	private List<String> bigramList;
	
	private List<String> unigramList;
	
	private List<String> stopwordList ;

//	private static final String UNIGRAM_PATH = "./data/Spanish_Data/output/unigram/" ;
	private static  String NGRAM_PATH  ;
	
	private static final String STOPWORD_PATH = "./data/config" ;
	
	private static final String STOPWORD_FILE = "stopwords.txt" ;
	
	private static final String DEFAULT_NUMBER = "22" ;

	
	private EXPERIMENT_MODE exprMode ;

	private unigramTypeEnum unigramTypeEnum;


	private HashSet<String> allpmiWords;

/*
	private enum EXPR
	{
		OLD, NEW 
	}
*/

	
	
	
	public BoWFeatureLoader(EXPERIMENT_MODE mode, SarcasmResourceLoader sarcasmRLObject 
			) throws IOException
	{
		
		unigramList = new ArrayList<String>(); 
		this.sarcasmRLObject = sarcasmRLObject ;
		unigramMap = new HashMap<String,Integer>();
		bigramList = new ArrayList<String>();
	
		loadKeyHashTags() ;
		this.exprMode = mode ;
		
		stopwordList = new 
				 ArrayList<String>();
		
		loadStopWords();
	
	/*	
		EXPR exprMode = EXPR.OLD ;
		
		if ( exprMode.equals(EXPR.OLD))
		{
			loadUnigrams();
		}
		*/	
	}
	
	public void setUnigramFile ( String UNIGRAM_FILE, String UnigramPath)
	{
		NGRAM_PATH = UnigramPath ;
		
		sarcasmRLObject.setUnigramFile(UNIGRAM_FILE, NGRAM_PATH) ;
		
		//doing rough code for WSD based sarcasm detection
		//using two files generated from pmi scores to use in the classifier
		//this is certainly an one time thing for a specific work
//		loadPMIFiles() ;
	}
	
	public void setBigramFile ( String BIGRAM_FILE, String BigramPath)
	{
		NGRAM_PATH = BigramPath ;
		
		sarcasmRLObject.setBigramFile(BIGRAM_FILE, NGRAM_PATH) ;
		
		//doing rough code for WSD based sarcasm detection
		//using two files generated from pmi scores to use in the classifier
		//this is certainly an one time thing for a specific work
//		loadPMIFiles() ;
	}
	
	public void setNgramFileType(unigramTypeEnum type) 
	{
		// TODO Auto-generated method stub
		this.unigramTypeEnum = type ;

	}
	
	public void loadPMIFiles()
	{
		allpmiWords = new HashSet<String>() ;
		String path = "./data/twitter_corpus/wsd/" ;
		String sarcFile ="tweet.SARCASM.good.context.txt.ppmi" ;
		String nonsarcFile ="tweet.NON_SARCASM.good.context.txt.ppmi" ;
		
		List<String >allSarcasmData = null ;
		try {
			allSarcasmData = Files.readAllLines(Paths.get(path + "/" + sarcFile),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = 0 ;
		for ( String sarcasm : allSarcasmData )
		{
			String features[] = sarcasm.split("\t") ;
			String utterance = features[1].trim() ;
			allpmiWords.add(utterance) ;
			i++ ;
			if ( i == 500)
			{
				break ;
			}
		}
		
		i = 0 ;
		try {
			allSarcasmData = Files.readAllLines(Paths.get(path + "/" + nonsarcFile),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for ( String sarcasm : allSarcasmData )
		{
			String features[] = sarcasm.split("\t") ;
			String utterance = features[1].trim() ;
			allpmiWords.add(utterance) ;
			
			i++ ;
			if ( i == 500)
			{
				break ;
			}
		}
		
	}
	
	
	public void setBigramFile ( String BIGRAM_FILE)
	{
	}
	
	public void loadKeyHashTags()
	{
		//english
		
		String [] englishHashtags = {"#sarcasm", "#sarcastic" , "#angry" , "#awful" , "#disappointed" ,
							  "#excited", "#fear" ,"#frustrated", "#grateful", "#happy" ,"#hate",
							  "#joy" , "#loved", "#love", "#lucky", "#sad", "#scared", "#stressed",
							  "#wonderful", "#positive", "#positivity", "#disappointed", "#irony"} ;
		
		//spanish
		String [] spanishHashtags = {"#sarcasmo", "#sarcasm" , "#feliz" , "#alegre" , "#entusiasmado" ,
				  "#contento", "#gratitud" ,"#diversion", "#amor", "#enamorado" ,"#optimismo",
				  "#triste" , "#enojado", "#irritado", "#aterrado", "#asustado", "#asustado", "#confundido"} ;
		
		
		hashTagList = new ArrayList<String>(Arrays.asList(spanishHashtags)) ;
		hashTagList.addAll(Arrays.asList(englishHashtags));
	}
	
	public void addDataToHashList(List<String> hashes)
	{
		hashTagList.addAll(hashes);
	}
	
	
	
	public void loadGlobalNgrams( String type ) throws IOException
	{
		sarcasmRLObject.loadGlobalNgrams(type)		;
//		sarcasmRLObject.loadNgrams(bigramList,UNIGRAM_PATH,BIGRAM_FILE)		;

	}
	
	public void loadLocalNgrams(String type) throws IOException
	{
		sarcasmRLObject.loadLocalNgrams(type)		;
//		sarcasmRLObject.loadNgrams(bigramList,UNIGRAM_PATH,BIGRAM_FILE)		;

	}
	
	
	

	@Override
	public Map<Integer,Double> loadFeatures(String[] tokens, String target) 
	{
		// TODO Auto-generated method stub
		Map<Integer, Double> bowMap = new HashMap<Integer, Double>();
		for (String token : tokens) 
		{
			//just a quick experiment to check if the words
			//with # has any effect on classification
	///		if (!allpmiWords.contains(token))
	//		{
	//			continue ;
	//		}
			
		//	if(token.equalsIgnoreCase("love") || token.equalsIgnoreCase("#love"))
		//	{
		//		continue ;
		//	}
			
			//we used to use the class StringUtils for stripping off NonAlphaNumeric chars
			//but probably we need to keep "#" or "@" as they are important for this type of problem
			//token = StringUtils.stripNonAlphaNumerics(token) ;
            token = StringEscapeUtils.escapeXml(token).trim();
            //if it is a number - convert to a default number
            
            if (token.startsWith("@"))
            {
            	token = "@user" ; // just converting to the same feature
            }
            
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
            if (stopwordList.contains(token) )
            {
                continue ;
            }
            //again do the trim
            token = token.trim();
            
            //we dont want to keep any word with hashtag that has been selected to retrieve the tweets
            //this is little tricky cause people may use hashtags in upper case!
            
           
            String lowerHashtags = token.toLowerCase();
			if(hashTagList.contains(lowerHashtags))
			{
				continue ;
			}
			
			if(token.equalsIgnoreCase(target) || token.equalsIgnoreCase("#"+target))
			{
				continue ;
			}
			
			if(lowerHashtags.equalsIgnoreCase("sarcasm") || lowerHashtags.equalsIgnoreCase("sarcastic") ||
					lowerHashtags.equalsIgnoreCase("#sarcasm") || lowerHashtags.equalsIgnoreCase("#sarcastic"))
			{
				continue ;
			}
			
			 if(token.length()<2) 
			 {
				 continue ;
			 }
			
			int index = getBOWIndex(token,exprMode);
			if (index != -1 )
			{
				Double old = bowMap.get(index);
				if ( null == old )
				{
					old = 0.0 ;
				}
				
				bowMap.put(index, old+1.0) ;
			}
			
		}
		return bowMap ;
	}
	
	private String doAllProcessing ( String token )
	{
        if (token.startsWith("@"))
        {
        	token = "@user" ; // just converting to the same feature
        	return token ;
        }		
		//we used to use the class StringUtils for stripping off NonAlphaNumeric chars
		//but probably we need to keep "#" or "@" as they are important for this type of problem
		//token = StringUtils.stripNonAlphaNumerics(token) ;
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
//        	System.out.println("here") ;
        }
        
        ret  = TextUtility.CheckNumeric(token);
        if (ret)
        {
        	token = DEFAULT_NUMBER ; // 22
        }
        
        //remove any stop word
        if (stopwordList.contains(token) )
        {
            return null ;
        }
        //again do the trim
        token = token.trim();
        return token ;
 
	}
	
	
	@Override
	public Map<Integer,Double> loadFeatures(String[] tokens) 
	{
		// TODO Auto-generated method stub
		Map<Integer, Double> bowMap = new HashMap<Integer, Double>();
		for (String token : tokens) 
		{
			//just a quick experiment to check if the words
			//with # has any effect on classification
			token = doAllProcessing(token) ;
			if ( null == token )
			{
				continue ;
			}
			
             String lowerHashtags = token.toLowerCase();
			if(hashTagList.contains(lowerHashtags))
			{
				continue ;
			}
			
			if(lowerHashtags.equalsIgnoreCase("sarcasm") || lowerHashtags.equalsIgnoreCase("sarcastic") ||
					lowerHashtags.equalsIgnoreCase("#sarcasm") || lowerHashtags.equalsIgnoreCase("#sarcastic"))
			{
				continue ;
			}
			
			 if(token.length()<2) 
			 {
				 continue ;
			 }
			
			int index = getBOWIndex(token,exprMode);
			if (index != -1 )
			{
				Double old = bowMap.get(index);
				if ( null == old )
				{
					old = 0.0 ;
				}
				
				bowMap.put(index, old+1.0) ;
			}
			
		}
		return bowMap ;
	}
	
	
	
	private int getBigramIndex(String bigram) 
	{
		// TODO Auto-generated method stub
		
		if (this.unigramTypeEnum == unigramTypeEnum.LOCAL)
		{
			
			if ( !bigramList.contains(bigram) )
			{
				bigramList.add(bigram) ;
			}
			
			//we need to select either of the one (unigram or bigram) 
			//since we cannot dynamically adjust the # of unigrams
			return sarcasmRLObject.getNonLexFeatureSize() + bigramList.indexOf(bigram) ;
		}
			
		
		//made a change here since it is easy to maintain the nonlexfiles as 0-100 indexes 
		//and then add the BoW
		
		//same thing will work for adding WP size...
		
		Integer index =  sarcasmRLObject.getBigramIndex(bigram) + sarcasmRLObject.getBoWsize() + sarcasmRLObject.getNonLexFeatureSize() ;
		return index ; 
	}
	
	
	private int getBOWIndex(String token, EXPERIMENT_MODE mode) 
	{
		// TODO Auto-generated method stub
		Integer index = -1 ;
		if (this.unigramTypeEnum == unigramTypeEnum.LOCAL)
		{
		
			index = sarcasmRLObject.getTokenIndex(token,this.unigramTypeEnum) + sarcasmRLObject.getNonLexFeatureSize()  ;
		}
		else
		{
			index = sarcasmRLObject.getTokenIndex(token) + sarcasmRLObject.getNonLexFeatureSize()  ;

		}

		return index ; 
	}
	
	public void writeNonNGramFeatures() throws IOException 
	{
		// TODO Auto-generated method stub
		{
			//? who write this?
		//	sarcasmRLObject.close(UNIGRAM_PATH,UNIGRAM_FILE,unigramMap);
		}
//		sarcasmRLObject.close(UNIGRAM_PATH,BIGRAM_FILE,bigramList);
		
	}
	
	public void close( String specialFile) throws IOException 
	{
		// TODO Auto-generated method stub
		{
			
	//		sarcasmRLObject.close(UNIGRAM_PATH,specialFile,unigramList);
		}
//		sarcasmRLObject.close(UNIGRAM_PATH,BIGRAM_FILE,bigramList);
		
	}

	
	//generating bigrams - can ignore
	public Map<Integer, Double> generateBigrams(String[] tokens) 
		{
			// TODO Auto-generated method stub
			Map<Integer,Double> bigramMap = new HashMap<Integer,Double>() ;
			for ( int i = 0 ; i < tokens.length-1 ; i++ )
			{
				String word_1 = tokens[i] ;
				word_1 = doAllProcessing(word_1) ;
				if ( null == word_1)
				{
					continue;
				}
				String word_2 = tokens[i+1] ;
				word_2 = doAllProcessing(word_2) ;
				if ( null == word_2)
				{
					continue;
				}
					
				String bigram = word_1 + "|||" + word_2 ;
				bigram = bigram.toLowerCase();
				Integer index = getBigramIndex(bigram) ;
				if (index != -1 )
				{
					Double old = bigramMap.get(index);
					if ( null == old )
					{
						old = 0.0 ;
					}
						
					bigramMap.put(index, old+1.0) ;
				}

				
			}
			
			
			return bigramMap;
		}


	public int getUnigramListSize() 
	{
		// TODO Auto-generated method stub
		int size = unigramMap.size();
		return size ;
	}

	public void loadSelectiveParaphraseFile () throws IOException
	{
		String path = "/Users/dg513/work/eclipse-workspace/nyucourse-workspace/NYUCourse/data/project/eval/AMTResults/" ;
		
		File file = new File ( path);
		
		File files[] = file.listFiles() ;
		
		BufferedReader reader = null ;
		
		for ( File f : files )
		{
			//moses op
			if (f.getName().contains("Batch_1377726_Moses_OP_AMT_votes.txt")  )
	//  f.getName().contains("Batch_1377909_batch_results_IBM2_OP_votes.txt") )
		//	if ( (f.getName().contains("ibm2_op_AMTResults_vote.txt")) || (f.getName().contains("moses_op_AMTResults_vote.txt")) )
			{
				reader = new BufferedReader ( new FileReader ( path + "/" + f.getName())) ;
				String header = reader.readLine() ;
				
				while ( true )
				{
					String line = reader.readLine() ;
					if ( null == line )
					{
						break;
					}
					String features[] = line.split("\t") ;
					String sarcasm = features[3] ;
					
					//4 -> previous (NYU) file format
				//	String choice = features[4] ;
					String choice = features[5] ;
					choice = choice.toLowerCase() ;
					if ( !choice.equalsIgnoreCase("choice2")) //choice2 is antonym
					{
						continue ;
					}
					
					String posn = features[4];
					if(!posn.equalsIgnoreCase("TOP"))
					{
						continue ;
					}
					
					features = sarcasm.split("\\s++") ;
					
					for ( int i = 0 ; i < features.length ; i++ )
					{
						String word_1 = StringUtils.stripNonAlphaNumerics(features[i]) ;
						
						word_1 =  StringEscapeUtils.escapeXml(features[i]);
						word_1 = features[i].toLowerCase();
					
						for ( int j = i+1 ; j < features.length; j++)
						{
							String word_2 = StringUtils.stripNonAlphaNumerics(features[j]) ;
							
							word_2 =  StringEscapeUtils.escapeXml(features[j]);
							word_2 = features[j].toLowerCase();
					
							
							String bigram = word_1 + "|||" + word_2 ;
							if (!bigramList.contains(bigram) )
							{
								bigramList.add(bigram);
							}
						}
					}
					
				}
				
				reader.close();
			}
		}
		
	}

	public void setUnigramTypeEnum(
			unigramTypeEnum type) 
	{
		// TODO Auto-generated method stub
		this.unigramTypeEnum = type ;
		
	}
	
	public void loadStopWords( ) throws IOException
	{
		BufferedReader reader = new BufferedReader ( new FileReader ( STOPWORD_PATH + "/" + STOPWORD_FILE) ) ;
		
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break;
			}
			stopwordList.add(line.trim()) ;
			
		}
		
		reader.close() ;
	}

	@Override
	public Map<String, Double> loadNonLexFeatures(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
