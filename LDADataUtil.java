/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.LDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 *
 *
 */

public class LDADataUtil
{

    private LDADataUtil() {}

    public static SortedSet<LDADataPair> fromString(String ldaData)
    {
        String[] split = ldaData.split("[|]");
        SortedSet<LDADataPair> pairs = new TreeSet<LDADataPair>();
        for (String s : split)
        {
            String[] dataPair = s.split("[=]");
            pairs.add(new LDADataPair(Integer.parseInt(dataPair[0]), Float.parseFloat(dataPair[1])));
        }
        return pairs;
    }

    public static String toString(SortedSet<LDADataPair> data)
    {
        StringBuilder sb = new StringBuilder();
        for (LDADataPair ldaDataPair : data)
        {
            sb.append(ldaDataPair.getKey() + "=" + ldaDataPair.getScore()).append("|");
        }
        return sb.toString();
    }
    
    public static List<String> readLDAInput ( File input ) throws IOException
    {
    	List<String> debateSpeeches = getDebateSpeeched(input);
		
    	return debateSpeeches ;
    }
    
    
	public static CharSequence[] readLdaInput(File inputDir) throws IOException 
	{
		File[] files = inputDir.listFiles();
		List<String> bodyTexts = new ArrayList<String>();
		if (files != null) 
		{
			for (File file : files) 
			{
				
				//discard the Ds_Store and other zip files 
				if (file.isDirectory())
				{
					continue ;
				}
				
				if(file.toString().contains("zip") || file.toString().contains("Store"))
				{
					continue ;
				}
				
				System.out.println(" debate file is " + file ) ;
				
				List<String> debateSpeeches = getDebateSpeeched(file);
				for (String doc : debateSpeeches) 
				{
					bodyTexts.add(doc);
				}
			}
		}
		CharSequence[] text = new CharSequence[bodyTexts.size()];
		return bodyTexts.toArray(text);
	}
	
	public static CharSequence[] readCrimeLdaInput(File inputDir) throws IOException 
	{
		File[] files = inputDir.listFiles();
		List<String> bodyTexts = new ArrayList<String>();
		if (files != null) 
		{
			for (File file : files) 
			{
				
				//discard the Ds_Store and other zip files 
				if (file.isDirectory())
				{
					continue ;
				}
				
				if(file.toString().contains("zip") || file.toString().contains("Store"))
				{
					continue ;
				}
				
				System.out.println(" debate file is " + file ) ;
				
				List<String> Tweets = getCrimeTweets(file);
				for (String doc : Tweets) 
				{
					bodyTexts.add(doc);
				}
			}
		}
		CharSequence[] text = new CharSequence[bodyTexts.size()];
		return bodyTexts.toArray(text);
	}

	public static List<String> getDebateSpeeched(
			File file) throws IOException 
			{
		// need to implement
		List<String> speeches = new ArrayList<String>() ;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( file) , "UTF8") );
		
		//header 
		String line = reader.readLine() ;
		while ( true )
		{
			line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			
			String features[] = line.split("\t");
			String debate = features[8] ;
			speeches.add(debate);
		}
		
		reader.close();
		
		return speeches;
	}

	public static List<String> getCrimeTweets(
			File file) throws IOException 
			{
		// need to implement
		List<String> utterances = new ArrayList<String>() ;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( file) , "UTF8") );
		
		//header 
	//	String line = reader.readLine() ;
		String tweet = null ;
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			
			String features[] = line.split("\t");
			
			if ( file.getName().endsWith("tweet"))
			{
				tweet = features[0] ;
			//	continue;
			}
			else
			{
				tweet = features[3] ;
			}
			//more filters - we do not want both 
			boolean ret = checkCountOfHashtags(tweet);
			if ( ret)
			{
				utterances.add(tweet);
			}
		}
		
		reader.close();
		
		return utterances;
	}
	
	private static boolean checkCountOfHashtags (String tweet)
	{
		int count = 0  ;
		tweet = tweet.toLowerCase() ;
		if ( tweet.contains("#crimingwhilewhite"))
		{
			count++ ;
		}
		
		if ( tweet.contains("#alivewhileblack") || tweet.contains("#blacklivesmatter"))
		{
			count++ ;
		}
		
		if ( count > 1 )
		{
			return false ;
		}
		else if ( count == 1 )
		{
			return true ;
		}
		else
		{
			return false ;
		}
	}

	
	public static List<String> getDebateSpeechesWithPartyVotes(
			File file) throws IOException 
			{
		// need to implement
		List<String> speeches = new ArrayList<String>() ;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( file) , "UTF8") );
		
		//header 
		String line = reader.readLine() ;
		while ( true )
		{
			line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			
			String features[] = line.split("\t");
			String party = features[4] ;
			String vote = features[6] ;
			String debate = features[8] ;
			String pvdebate = party + "|" + vote + "|" + debate  ;
			
			if ( debate.length() > 200 )
			{
				speeches.add(pvdebate);
			}
		}
		
		reader.close();
		
		return speeches;
	}
	
	public static List<String> getDebateSpeechesWithPartyVotesAndReps(
			File file) throws IOException 
			{
		// need to implement
		List<String> speeches = new ArrayList<String>() ;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( file) , "UTF8") );
		
		//header 
		String line = reader.readLine() ;
		while ( true )
		{
			line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			
			String features[] = line.split("\t");
			String speaker = features[1] ;
			String party = features[4] ;
			String vote = features[6] ;
			String debate = features[8] ;
			String pvdebate = speaker + "|" + party + "|" + vote + "|" + debate  ;
			
			if ( debate.length() > 200 )
			{
				speeches.add(pvdebate);
			}
		}
		
		reader.close();
		
		return speeches;
	}


}
