package com.rutgers.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrTokenizer;

public class Tokenization 
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	private static PorterStemmerWest stemmer = new  PorterStemmerWest();
	
	private static TokenizerModel model;

	private static TokenizerME tokenizer;
	
	private static final String OPENNLP_TOKENFILE = "en-token.bin" ;
	
	private static Counter<String> unigrams =  new Counter<String>();
	
	private static CMUTokenization cmuTokenizer  = new CMUTokenization();
	
	private static final String CONFIG = "./data/config/" ;
	
	private static final String STOP_FILE = "stopwords_small.txt" ;
	
	private static final String EXCEPTION_FILE = "exceptionWord.lst" ;
	
	private static HashSet<String> allUtterances ;
	 
	
	private static String [] hashtags = {"#sarcasm", "#sarcastic" , "#angry" , "#awful" , "#disappointed" ,
			  "#excited", "#fear" ,"#frustrated", "#grateful", "#happy" ,"#hate",
			  "#joy" , "#loved", "#love", "#lucky", "#sad", "#scared", "#stressed",
			  "#wonderful", "#positive", "#positivity", "#disappointed", "#irony"} ;

	private static HashSet<String> uniqueTweets;


	public static List<String> loadStopWords() throws IOException
	{
		List<String >stopList = new ArrayList<String>() ;
		BufferedReader reader = new BufferedReader ( new FileReader (CONFIG + "stopwords_small.txt"));
		
		while (true )
		{
			String line = reader.readLine();
			if ( null == line )
			{
				break;
			}
			stopList.add(line.trim());
			
		}
		
		reader.close();
		return stopList ;
	}
	
	public static void initStemmer() throws IOException
	{
		String exception = CONFIG + "/" + EXCEPTION_FILE ;
		stemmer.initExceptionWords(exception) ;
		
		String stopwords = CONFIG + "/" + STOP_FILE ;
		stemmer.initStopWords(stopwords) ;
	
	}
	
	public static void tokenization(String input, String output,
			String opFile, boolean stem) throws IOException
	{
		allUtterances = new HashSet<String>() ;
		
		FileInputStream nlpStream = new FileInputStream(CONFIG + OPENNLP_TOKENFILE );
	//	model = new TokenizerModel(nlpStream);
	//	tokenizer = new TokenizerME(model);
	
		BufferedWriter writer1 = new BufferedWriter ( new OutputStreamWriter ( 
				new FileOutputStream ( output + "/" + opFile + ".tokens" ), "UTF8")) ;
		
		BufferedWriter writer2 = new BufferedWriter ( new OutputStreamWriter ( 
				new FileOutputStream ( output + "/" + opFile + ".tokens.unigrams" ), "UTF8")) ;
	
		
		
		File file = new File ( input ) ;
		File[] files = file.listFiles() ;
		
		
		for ( File f : files )
		{
			if (f.isDirectory())
			{
				continue ;
			}
			if ( f.getName().startsWith("Store"))
			{
				continue ;
			}
			
		//	if ( !f.getName().endsWith("filtered"))
		//	{
		//		continue ;
		//	}
			
			tokenization(input,f.getName(),writer1,writer2,stem) ;
		}
		
		writer1.close() ;
		
		System.out.println("Finished tokenization") ;
		
		for ( String unigram : unigrams.keySet())
		{
			writer2.write(unigram + "\t" + unigrams.getCount(unigram));
			writer2.newLine() ;
		}
		
		
		writer2.close();
	
	}
	
	public static void createUnigrams(String input, String output, String opFile) throws IOException
	{
	
		//this is for training/testing issues
		//create the unigram counts for training data (sarcasm and positive) 
		
		File f = new File ( input ) ;
		File[] files = f.listFiles() ;
		
		BufferedReader reader = null ;
		
		BufferedWriter writer2 = new BufferedWriter ( new OutputStreamWriter ( 
				new FileOutputStream ( output + "/" + opFile  ), "UTF8")) ;
	
		
		
		 uniqueTweets = new HashSet<String>() ;
	
		
		for ( File file : files )
		{
			if ( file.getName().contains("Store"))
			{
				continue ;
			}
			//tweet.sarcasm.filtered.031012015.lemma.emoji.targets.all
			
			if (  file.getName().endsWith("lemma.emoji.targets.all"))
			{
				
				reader = new BufferedReader ( new InputStreamReader ( 
						new FileInputStream ( input + "/" + file.getName() ),"UTF8")  );
				
				System.out.println("file reading is " + file.getName()) ;
				
				
				while ( true )
				{
					String line = reader.readLine();
					if ( null == line )
					{
						break ;
					}
					
					String features[] = line.split("\t") ;
					String snum = features[1] ;
					String utterance = features[2] ;
					
					if (!uniqueTweets.contains(snum))
					{
						uniqueTweets.add(snum);
					}
					else
					{
						continue ;
					}
					
					features = utterance.split("\\s++") ;
					
					for ( String feature : features )
					{
						unigrams.incrementCount(feature, 1.0);
					}
				}
				
				reader.close();
			}
		}
		
		System.out.println("Finished tokenization") ;
		
		double idfNeum = (double) uniqueTweets.size() ;
		for ( String unigram : unigrams.keySet())
		{
			//we can calculate the idf here 
			//lets consider for each tweet the number of entries for the unigram is only once
			//so the count of it will show the denom of idf
			double idfDenom = unigrams.getCount(unigram) ;
			double idf = Math.log(idfNeum/idfDenom) ;
			writer2.write(unigram + "\t" + idfDenom + "\t" + idf);
			writer2.newLine() ;
		}
		
		
		writer2.close();
	}
	
	
	public static void tokenization(String path, String file,
			BufferedWriter writer1, BufferedWriter writer2, boolean stem) throws IOException
	{
		BufferedReader reader = new BufferedReader ( new InputStreamReader ( 
				new FileInputStream ( path + "/" + file ),"UTF8")  );
		
		System.out.println("READING FILE: " + file);
		
		List<String> stopList = loadStopWords() ;
		
		while ( true )
		{
			String message = reader.readLine() ;
			if ( null == message )
			{
				break ;
			}
		
			String line = message ;
		//	String snum = message.split("\t")[1] ;
		//	String hash = message.split("\t")[4] ;
	//		String line = message.split("\t")[5] ;
			
	//		line = "Thanks #TimeWarner for being on the fritz, I didn't want to see #ChicagoFire anyway... #NOT" ;
			
	//		System.out.println(line) ;
			
		//	StrTokenizer tokenizer = new StrTokenizer(line) ;
			line = StringUtils.stripAccents(line) ;
			line = TextUtility.removeHashes(line,hashtags);
			
		//	StringTokenizer tokenizer = new StringTokenizer(line.trim()); 
	
			//probably the CMU one or the Stanford glove are the best tokenizers for tweets
			List<String> tokens = cmuTokenizer.tokenizeRawTweetText(line);
			
			StringBuffer buffer = new StringBuffer() ;
			for ( String token : tokens )
			{
				if(stopList.contains(token.trim()))
				{
					continue ;
				}
				
				if (token.startsWith("@"))
				{
					token = "@ToUser" ;
				}
				boolean number = NumberUtils.isNumber(token);
				if(number)
				{
					token = "22" ; //all numbers are 22!
				}
				
				if (token.startsWith("http") || token.startsWith("HTTP") )
				{
					token = "URL" ;
				}
				
				if(token.startsWith("://"))
				{
					continue ;
				}
				
			//	if( TextUtility.CheckNonAlpha(token) )
				{
					//if we use CMU tokenizer - may be we dont have to do this?
				//	token = TextUtility.stripNonAlpha(token);
				}
				
				//if all the characters are UPPER CASE - keep that
				if(!TextUtility.checkUppercase(token))
				{
					token = token.toLowerCase();
				}
				
			//	if (token.length() < 2)
			//	{
			//		continue ;
			//	}
				
		//		token = "yeaaaaaaaaaaaaaaaaaaaah" ;
		//		token = "yeaaah" ;
		//		Pattern p = Pattern.compile("(((\\w)\\3+)+)+");
		//		Matcher m = p.matcher(token);
		//		if (m.find())
		//		{
		//		    System.out.println("Duplicate character " + m.group(1));
		//		} 
				//now stem it
		//		System.out.println(token) ;
			//	token = "yeaaaaaaaaaaaaaaaaaaaah" ;
			//	token ="suuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuureeeee" ;
				if(stem)
				{
					token = stemmer.stemWord(token); 
				}
				
				buffer.append(token);
				buffer.append(" ");
			}
			
			String ret = buffer.toString().trim() ;
		//	System.out.println(ret);
			
			//dont write the buffer directly as there are too many repeats!!!
			//we can store everything in a hashset now and write later
			if(!allUtterances.contains(ret))
			{
				allUtterances.add(ret);
				
				//do a simple split on //s++
				//little clumsy code but will work!
				String finalTokens[] = ret.split("\\s++") ;
				for ( String token : finalTokens )
				{
					unigrams.incrementCount(token, 1.0);
				}
			//	writer1.write(snum + "\t" + ret);
				writer1.write(ret);
				writer1.newLine();
			}
			else
			{
		//		System.out.println("present~") ;
			}
			

		}
		reader.close();
	}
	
	private static String getString(String[] outputs) 
	{
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer() ;
		for ( String output : outputs )
		{
			
			buffer.append(output);
			buffer.append(" ") ;
		}
		
		return buffer.toString().trim();
	}

	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
	//	String input = "./data/input/wordnet_input/";
	//	String output = "./data/input/wordnet_tokens/";
	//	String file = "text.wem" ;
		
	//	String input = "./data/input/positive_training/" ;
	//	String output = "./data/input/positive_tokens/" ;
	//	String opFile = "tweet.positive.03032015.training.lemma.emoji.unigrams" ;
	
	//	String input = "./data/input/positive_training/" ;
		String input = "./data/input/sarcasm_tokens/" ;
		String output = input ;
		String opFile = "tweet.sarcasm.03182015.training.lemma.emoji.unigrams" ;
	
		// tweet.sarcasm.03032015.training.lemma.emoji.unigrams 

		
		boolean stem = false ;
		if ( stem)
		{
			initStemmer() ;
		}
		
		
		
	//	tokenization(input,output,file,stem) ;
		createUnigrams(input,output,opFile);
	}

}
