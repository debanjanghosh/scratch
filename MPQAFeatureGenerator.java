package com.deft.sarcasm.features;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPQAFeatureGenerator 
{
	private static final String mpqaPath =  "./data/mpqa/" ;
	private static final String liwcPath =  "./data/twitter_corpus/english_dictionary/" ;
	private static final String opinionPath =  "./data/opinion-lexicon-English/" ;
	
	
	
	private String mpqaFile =  "subjclueslen1-HLTEMNLP05.tff" ;
	private String liwcFile =  "19.csv" ;
	private String opinionPositiveFile = "positive-words-noheader.txt" ;
	private String opinionNegativeFile = "negative-words-noheader.txt" ;
	
	
	
	private HashMap<String, List<String>> typeWordMap;
	private List<String> negated ;
	private List<String> positive_opinions ;
	private List<String> negative_opinions ;
	
	
	public MPQAFeatureGenerator()
	{
		typeWordMap = new HashMap<String,List<String>>() ;
		negated = new ArrayList<String>() ;
	}
	
	public void init() throws IOException
	{
		loadMPQA();
		loadNegatedLIWC();
		loadOpinionLexicon();
	}
	
	public void loadOpinionLexicon() throws IOException
	{
		positive_opinions = Files.readAllLines(Paths.get(opinionPath + opinionPositiveFile) , StandardCharsets.UTF_8);
		negative_opinions = Files.readAllLines(Paths.get(opinionPath + opinionPositiveFile) , StandardCharsets.UTF_8);
		
		
	}
	
	public void loadNegatedLIWC() throws IOException
	{
		//load liwc 
		BufferedReader reader = new BufferedReader ( new FileReader ( liwcPath + "/" + liwcFile )) ;
		List<String> words = null ;
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			
			negated.add(line);
		}
		
		reader.close();
		
	}
	
	
	public void loadMPQA() throws IOException
	{
		
		//load mpqa 
		BufferedReader reader = new BufferedReader ( new FileReader ( mpqaPath + "/" + mpqaFile )) ;
		List<String> words = null ;
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			String features[] = line.split("\\s++") ;
			
//			type=strongsubj len=1 word1=abuse pos1=verb stemmed1=y priorpolarity=negative
			String word = features[2].split("=")[1];
			String polarity = null ;
			if ( features.length == 6)
			{
				polarity = features[5].split("=")[1];
			}
			if ( features.length == 7)
			{
				polarity = features[6].split("=")[1];
			}
			
			words = typeWordMap.get(polarity) ;
			if ( null == words )
			{
				words = new ArrayList<String>() ;
			}
			words.add(word);
			//if (word.equalsIgnoreCase("awesome"))
		//	{
		//		System.out.println("here");
		//	}
			//the problem is in stemming!
			//shall we stem the words here too? 
		//	if(word.equalsIgnoreCase("Meritor"))
		//	{
		//		System.out.println("here");
		//	}
			
		//	word = tokenizerObj.stem(word);
			
			
			words.add(word);
			
		//	if(word.equalsIgnoreCase("Meritor"))
		//	{
		//		System.out.println("here");
		//	}
			
			typeWordMap.put(polarity, words);
		}
		
		reader.close();
	}
	
	//tokenized and stemmed words
	public Map<String,Double> extractSentiFeatures ( List<String> words1, List<String> words2 )
	{
		Map<String,Double> featureMap = new HashMap<String,Double>() ;
		
	
		List<Double> sentiments1 = getSentiments(words1);
		List<Double> sentiments2 = getSentiments(words2);
		
		featureMap.put("totalArg1Positive", sentiments1.get(0)) ;
		featureMap.put("totalArg1Negative", sentiments1.get(1)) ;
		featureMap.put("totalArg1Neutral", sentiments1.get(2));
		featureMap.put("totalArg1NegatePositive", sentiments1.get(3));
		featureMap.put("totalArg2Positive", sentiments2.get(0)) ;
		featureMap.put("totalArg2Negative", sentiments2.get(1)) ;
		featureMap.put("totalArg2Neutral", sentiments2.get(2)) ;
		featureMap.put("totalArg2NegatePositive", sentiments2.get(3)) ;

		return featureMap ;
	}
	
	//tokenized and stemmed words
	public Map<String,Double> extractSentiFeatures ( List<String> words1 )
	{
		Map<String,Double> featureMap = new HashMap<String,Double>() ;
		
	
		List<Double> sentiments1 = getSentiments(words1);
		
		featureMap.put("totalArg1Positive", sentiments1.get(0)) ;
		featureMap.put("totalArg1Negative", sentiments1.get(1)) ;
		featureMap.put("totalArg1Neutral", sentiments1.get(2));
		featureMap.put("totalArg1NegatePositive", sentiments1.get(3));
		return featureMap ;
	}
	
	public List<Double> getSentiments( List<String> words1)
	{
		List<Double> sentiments = new ArrayList<Double>() ;
		
		double totalArgPositive = 0.0 ;
		double totalArgNegative = 0.0 ;
		double totalArgNeutral = 0.0 ;
		double totalArgNegatePositive = 0.0 ;
		
		for (int i = 0 ; i < words1.size() ; i++ )
		{
			String word = words1.get(i);
			
			//check positive
			if(checkPositiveSentiment(word))
			{
				//check if negated
				if(negated(i,words1))
				{
					totalArgNegatePositive++ ;
				}
				else
				{
					totalArgPositive++ ;
				}
			}
			//check neutral
			if(checkNeutralSentiment(word))
			{
				totalArgNeutral++ ;
			}
			//check negative
			if(checkNegativeSentiment(word))
			{
				totalArgNegative++ ;
			}
		}
		
		sentiments.add(totalArgPositive);
		sentiments.add(totalArgNegative);
		sentiments.add(totalArgNeutral);
		sentiments.add(totalArgNegatePositive);
		
		return sentiments ;
		
	}
	
	private boolean negated(int posn, List<String> words) 
	{
		// TODO Auto-generated method stub
		for ( int i = 0 ; i < posn ; i++)
		{
			String word = words.get(i);
			if ( negated.contains(word))
			{
				return true ;
			}
		}
		return false;
	}
	
	private boolean checkNegativeSentiment (String word )
	{
		List<String> negatives = typeWordMap.get("negative") ;
		if ( negatives.contains(word))
		{
			return true ;
		}
		
		if(negative_opinions.contains(word))
		{
			return true ;
		}
		
		return false ;
	}

	private boolean checkNeutralSentiment (String word )
	{
		List<String> neutrals = typeWordMap.get("neutral") ;
		if ( neutrals.contains(word))
		{
			return true ;
		}
		
		return false ;
	}
	
	private boolean checkPositiveSentiment ( String word )
	{
		List<String> positives = typeWordMap.get("positive") ;
		if ( positives.contains(word))
		{
			return true ;
		}
		
		if(positive_opinions.contains(word))
		{
			return true ;
		}
		
		
		
		return false ;
	}
	//just test the functionality
/*	
	public static void main(String[] args) throws Exception 
	{
		MPQAFeatureGenerator mpqaObj = new MPQAFeatureGenerator();
		mpqaObj.init();
		
	}
*/	
}
