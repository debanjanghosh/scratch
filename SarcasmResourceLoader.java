package com.deft.sarcasm.train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.deft.sarcasm.features.EXPERIMENT_MODE;
import com.deft.sarcasm.train.SarcasmTrainHandler.unigramTypeEnum;


public class SarcasmResourceLoader 
{
	private Map<String,Set<String>> dictionaryMap ;
	private Map<String,Integer> bowMap ;
	private Map<String,Integer> wpMap ;

	
	
	private boolean DFFilter = false ;
//	private List<String> unigramList;
	private Map<Integer, Integer> bowPosnMap;
	private EXPERIMENT_MODE expr_mode;
	private ArrayList<String> nonLexList;
	
	private String UNIGRAM_FILE;
	private String UNIGRAM_PATH;
	
	private String BIGRAM_FILE;
	private String BIGRAM_PATH;
	
	private String nonLexFile;
	private static final int BUFFER_SIZE = 5;
	
	private static int MIN_DF = 3 ; //3 is default 
	
	private List<String> ngramTypes ;
	private unigramTypeEnum unigramTypeEnum; 
	
	private static final String BIGRAM = "WP" ;
	private static final String UNIGRAM = "BOW" ;
	
	public void setDFFilter( boolean filter )
	{
		this.DFFilter = filter ;
	}
	
	public void setMinimumDF ( int df )
	{
		this.MIN_DF = df ; 
	}
	

	public SarcasmResourceLoader()
	{
		nonLexList = new ArrayList<String>() ;
		bowMap = new HashMap<String,Integer>();
		bowPosnMap = new HashMap<Integer,Integer>();
		wpMap =  new HashMap<String,Integer>();
	}
	 
	public SarcasmResourceLoader(EXPERIMENT_MODE expr_mode) 
	{
		// TODO Auto-generated constructor stub
		nonLexList = new ArrayList<String>() ;
		this.expr_mode = expr_mode ;
		bowMap = new HashMap<String,Integer>();
		bowPosnMap = new HashMap<Integer,Integer>();
		wpMap =  new HashMap<String,Integer>();

		
	}

	public void loadGlobalNonLexFeatures (  ) throws IOException
	{		
		
		BufferedReader reader = new BufferedReader ( new InputStreamReader( 
				new FileInputStream ( UNIGRAM_PATH + "/" + nonLexFile ), "UTF8") );		
		
		int lineNum = 0 ;
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			String word = line.trim() ;
			
			nonLexList.add(word);
			
	//		unigramList.add(features[0].trim()) ;
			
			lineNum++ ;
		}
			
		reader.close() ;
		
	}
	
	public void setNGramFeatureTypes ( List<String> ngramTypes )
	{
		this.ngramTypes = ngramTypes ;
	}
	
	public void setUnigramFile ( String UNIGRAM_FILE, String UnigramPath)
	{
		this.UNIGRAM_FILE = UNIGRAM_FILE ;
		UNIGRAM_PATH = UnigramPath ;
	}
	
	public void setBigramFile ( String BIGRAM_FILE, String BigramPath)
	{
		this.BIGRAM_FILE = BIGRAM_FILE ;
		BIGRAM_PATH = BigramPath ;
	}

	
	public void setNonLexfile (String nonLexFile )
	{
		this.nonLexFile = nonLexFile ;
	}
	
	public int getTokenIndex ( String  token )
	{
		Integer val = bowMap.get(token) ;
		if ( null == val )
		{
			 return -1 ;
		}
		else
		{
			return val ;
		}
	}
	
	public int getBigramIndex ( String  token )
	{
		Integer val = wpMap.get(token) ;
		if ( null == val )
		{
			 return -1 ;
		}
		else
		{
			return val ;
		}
	}
	
	
	public void loadLocalNgrams ( String type ) throws IOException
	{		
		
		
		
		
		UNIGRAM_FILE = UNIGRAM_FILE + ".df_filtered." + MIN_DF + ".lst" ;
		System.out.println("Loading vocabulary: " + UNIGRAM_FILE  ) ;
		
		BufferedReader reader = new BufferedReader ( new InputStreamReader( 
				new FileInputStream ( UNIGRAM_PATH + "/" + UNIGRAM_FILE ), "UTF-8") );		
		
		//start linenumber from 1 so that liblinear can handle features like 0:1
		//recall the 0th index is "BLANK_FEATURE"
		int lineNum = 1;

		//blankline
	//	String header = reader.readLine() ;
		
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			String word = line.trim();
			bowMap.put(word,lineNum);
			lineNum++ ;
			
		}
			
		reader.close() ;
		
		System.out.println("Vocabulary size for unigrams (before filtering) is: " + lineNum ) ;
		System.out.println("Vocabulary size for unigrams (after filtering) is: " + bowMap.size()) ;
		
		//check if b
		
		
	}
	
	private void loadAllLinesForUnigrams ( List<String> lines  )
	{
		int lineNum = -1 ;
		for ( String line : lines)
		{
			lineNum++ ;
			String features[] = line.split("\t");
			if ( features.length !=2)
			{
				System.out.println("Error in global unigram file format - check " + lineNum);
				return ;
			}
		
			String word = line.split("\t")[0];
			Integer count = Integer.valueOf(line.split("\t")[1]);
			//filter by count
			if ( DFFilter)
			{
				if (count >= MIN_DF)
				{
					if ( bowMap.containsKey(word))
					{
						continue ;
					}
					bowMap.put(word,lineNum);
					bowPosnMap.put(lineNum,count) ;
				}
			}
			else
			{
				bowMap.put(word,lineNum);
				bowPosnMap.put(lineNum,count) ;
			}
		}
		
		System.out.println("Vocabulary size (before filtering) is: " + lineNum ) ;
		System.out.println("Vocabulary size (after filtering) is: " + bowMap.size()) ;

	}
	
	
	
	private void loadAllLinesForBigrams ( List<String> lines  )
	{
		int lineNum = -1 ;
		for ( String line : lines)
		{
			lineNum++ ;
			String features[] = line.split("\t");
			if ( features.length !=2)
			{
				System.out.println("Error in global unigram file format - check " + lineNum);
				return ;
			}
		
			String word = line.split("\t")[0];
		//	Integer count = Integer.valueOf(line.split("\t")[1]);
			//filter by count
			
			wpMap.put(word,lineNum);
		
		}
		
		System.out.println("Vocabulary size for bigrams (before filtering) is: " + lineNum ) ;
		System.out.println("Vocabulary size for bigrams (after filtering) is: " + bowMap.size()) ;

	}

	
	public void loadGlobalNgrams ( String type ) throws IOException
	{		
		
		if (type.equalsIgnoreCase(UNIGRAM))
		{
			System.out.println("Loading vocabulary: " + UNIGRAM_FILE) ;
			List<String> lines = Files.readAllLines(Paths.get(UNIGRAM_PATH + "/" + UNIGRAM_FILE), StandardCharsets.UTF_8 );
			loadAllLinesForUnigrams(lines);
		}
		
		else if (type.equalsIgnoreCase(BIGRAM))
		{
			System.out.println("Loading vocabulary: " + BIGRAM_FILE) ;
			List<String> lines = Files.readAllLines(Paths.get(BIGRAM_PATH + "/" + BIGRAM_FILE), StandardCharsets.UTF_8 );
			loadAllLinesForBigrams(lines) ;
		}

		
	}
	
	public List<String> getLexFeatures()
	{
		List<String> lexFeatures = new ArrayList<String>() ;
		int lexSizes = bowMap.size() ;
		for ( int i = 0 ; i < lexSizes ; i++)
		{
			lexFeatures.add("BLANK") ;
		}
		
		for ( String key : bowMap.keySet() )
		{
			int value = bowMap.get(key) ;
			lexFeatures.set(value, key);
			
 		}
		
		return lexFeatures ;
	}
	
	public List<String> getNonLexFeatures()
	{
		return nonLexList ;
	}
	
	
	public void writeNGramFile() throws IOException
	{
		BufferedWriter writer = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (UNIGRAM_PATH + "/" + UNIGRAM_FILE + ".df_filtered." + MIN_DF + ".lst" ), "UTF8")) ;
		
		int size = bowMap.size() ;
		List<String> unigrams = new ArrayList<String>() ;
		for (int i = 0 ; i <=size ; i++)
		{
			unigrams.add("BLANK_FEATURE") ;
		}
		
		for ( String key : bowMap.keySet())
		{
			int index = bowMap.get(key);
			unigrams.set(index, key) ;
		}
		
		for ( String unigram : unigrams)
		{
			writer.write(unigram);
			writer.newLine();
		}
		writer.close() ;
	}
	
	public void writeUnigramFile() throws IOException
	{
		BufferedWriter writer = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (UNIGRAM_PATH + "/" + UNIGRAM_FILE + ".df_filtered." + MIN_DF + ".lst" ), "UTF8")) ;
		
		int size = bowMap.size() ;
		List<String> unigrams = new ArrayList<String>() ;
		for (int i = 0 ; i <=size ; i++)
		{
			unigrams.add("BLANK_FEATURE") ;
		}
		
		for ( String key : bowMap.keySet())
		{
			int index = bowMap.get(key);
			unigrams.set(index, key) ;
		}
		
		for ( String unigram : unigrams)
		{
			writer.write(unigram);
			writer.newLine();
		}
		writer.close() ;
	}

	
	public void writeNonNgramFiles() throws IOException
	{
		BufferedWriter writer = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (UNIGRAM_PATH + "/" + nonLexFile ), "UTF8")) ;
		
		for ( String unigram : nonLexList)
		{
			writer.write(unigram);
			writer.newLine();
		}
		writer.close() ;
	}
	
	//this function load the dictionaries such as
	//LIWC, WordNet emotions, emoticons etc.
	//this dataset is received from the work of Roberto et al. (ACL 2011)
	//each file name shows the column
	@SuppressWarnings("unused")
	public void loadLIWCDictionaries( String path ) throws IOException
	{
		dictionaryMap = new HashMap<String,Set<String>>() ;
		Set<String> features = null ;
		String[] dictionaries = null;
		
	/*
		try
		{
			 dictionaries = Reader.getResourceListing(this.getClass(), path);
		}
		catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/	
		File dictFolder = new File (path) ;
		if(dictFolder != null) 
		{
			System.out.println("NOT NULL: " + dictFolder.getAbsolutePath());
		}
		else
		{
			System.out.println("Directory not present: " + dictFolder.getAbsolutePath());
			return ;
		}
		
		dictionaries = dictFolder.list() ;
		
		BufferedReader reader = null ;
		for ( String dictionary: dictionaries)
		{
			//for macs
			if(dictionary.contains("Store"))
			{
				continue ;
			}
		//	reader = new BufferedReader ( new InputStreamReader(Reader.findStreamInClasspathOrFileSystem(path + dictionary), "UTF8"));
			reader = new BufferedReader ( new InputStreamReader ( 
					new FileInputStream ( path + "/" + dictionary), "UTF8") );
			
			while ( true )
			{
				String line = reader.readLine() ;
				if ( null == line )
				{
					break ;
				}
				String feature = line.trim() ;
				
				features = dictionaryMap.get(dictionary) ;
				if ( null == features )
				{
					features = new HashSet<String>() ;
				}
				features.add(feature);
				dictionaryMap.put(dictionary,features) ;
			}
			
			reader.close() ;
		}					
	}

	public List<String> getListOfColumns(String token) 
	{
		// TODO Auto-generated method stub
		List<String> colList = new ArrayList<String> () ;
		
		for ( String dictionary : dictionaryMap.keySet() )
		{
			Set<String> dictTokens = dictionaryMap.get(dictionary) ;
		//	Integer column = parseColumnName(dictionary);
			
			
			for ( String dictToken : dictTokens )
			{
				dictToken = dictToken.toLowerCase().trim() ;
				token = token.toLowerCase().trim() ;
				
				if(dictToken.equals(token))
				{
					colList.add(dictionary);
				}
			/*	
				else
				{
					Pattern pattern = Pattern.compile(dictToken);
					Matcher matcher = pattern.matcher(token);
					if (matcher.find()) 
					{
						colList.add(dictionary);
					}
				}
			*/	
			}
		}
		return colList ;
	}

	private Integer parseColumnName(String dictionary) 
	{
		// TODO Auto-generated method stub
		int index = dictionary.indexOf(".");
		Integer name = Integer.valueOf(dictionary.substring(0,index));
		return name;
	}
	
	public void close(String path,String file,List<String> unigramList) throws IOException
	{
		BufferedWriter writer = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (path + "/" + file ), "UTF8")) ;
		
		for ( String unigram : unigramList)
		{
			writer.write(unigram);
			writer.newLine();
		}
		writer.close() ;
	}
	
	public void setExperimentMode ( EXPERIMENT_MODE expr_mode)
	{
		this.expr_mode = expr_mode ;
	}
	
	
	private int getNonLexPosition ( String nonWP)
	{
		int size = bowMap.size();
		
		//first check if the pair is already in non-wp list
		//we need to maintain two maps/list for words (lexical) and other features
		//because we simply cannot enter keys/values in an existing map
	/*	
		if (!nonLexList.contains(nonWP))
		{
			if(expr_mode.equals(EXPERIMENT_MODE.TRAINING))
			{
				nonLexList.add(nonWP);
			}
		}
	*/
		
		int index = nonLexList.indexOf(nonWP) ;
		if ( index == -1 )
			return index ;
		
		return (index ) ; 
				
	}

	public Map<Integer, Double> createNonNgramFeatures(
			Map<String, Double> nonLexicalMap) 
	{
		// TODO Auto-generated method stub
		Map<Integer,Double> tempMap = new HashMap<Integer,Double>() ;
		
		for ( String key : nonLexicalMap.keySet())
		{
			double value = nonLexicalMap.get(key);
			
			int position = getNonLexPosition(key);
			if (position != -1)
			{
				tempMap.put(position, value) ;
			}
		}
		
		
		return tempMap;
	}

	public int getNonLexFeatureSize() {
		// TODO Auto-generated method stub
		return nonLexList.size();
	}
	
	public int getBoWsize()
	{
		return bowMap.size();
	}
	
	public int getBigramsize()
	{
		return wpMap.size();
	}

	public int getTokenIndex(String token, com.deft.sarcasm.train.SarcasmTrainHandler.unigramTypeEnum unigramTypeEnum) 
	{
		// TODO Auto-generated method stub
		Integer val = bowMap.get(token) ;
		if ( null == val )
		{
			 int size = bowMap.size();
			 bowMap.put(token, size);
			 return bowMap.get(token) ;
		}
		else
		{
			return val ;
		}
		
	}

	public void setNgramFileType(com.deft.sarcasm.train.SarcasmTrainHandler.unigramTypeEnum type) 
	{
		// TODO Auto-generated method stub
		this.unigramTypeEnum = type ;
		if ( type.equals(unigramTypeEnum.LOCAL))
		{
			
			bowMap = new HashMap<String,Integer>();
			bowPosnMap = new HashMap<Integer,Integer>();
			wpMap =  new HashMap<String,Integer>();
		}
	}
}
