package com.research.course.debate.LDA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PerspectiveClassification 
{

	/**
	 * @param args
	 */
	
	private List<String> unigrams ;
	private List<String> bigrams ;
	private List<String> trigrams ;
	
	
	public PerspectiveClassification()
	{
		unigrams = new ArrayList<String>();
		bigrams = new ArrayList<String>();
		trigrams = new ArrayList<String>();
	}
	
	public void loadPerspectiveData ( String path, String file ) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( path + file) , "UTF8") );
		
		BufferedWriter writer = new BufferedWriter(new FileWriter ( path + file + ".fv") );
	
		
		//header
		String line = reader.readLine();
		
		while (true)
		{
			line = reader.readLine();
			if (line == null)
			{
				break;
			}
			
			String features[] = line.split("\t");
			String debate = ExtractDebateName(features[0].trim());
			String firstSpeech = features[2].trim().toLowerCase();
			firstSpeech = firstSpeech.substring(1,firstSpeech.length()-1);
			List<String> firstTopics = ExtractTopics (features[3]);
			String firstParty = firstSpeech.split("\\|")[0];
			String firstVote = firstSpeech.split("\\|")[1];
			firstSpeech = firstSpeech.split("\\|")[2];
			
			List<Integer> trigramFirstSpeechFeatures = generateFeatures(firstSpeech);
			String fv = generateFeatureString(trigramFirstSpeechFeatures);
			fv = convert(firstParty) + " " + fv ;
			System.out.println(fv);
			writer.write(fv);
			writer.newLine();
	
			String secondSpeech = features[4].trim().toLowerCase() ;
			secondSpeech = secondSpeech.substring(1,secondSpeech.length()-1);
			
			List<String> secondTopics = ExtractTopics (features[5]);
			String secondParty = secondSpeech.split("\\|")[0];
			String secondVote = secondSpeech.split("\\|")[1];
			secondSpeech = secondSpeech.split("\\|")[2];
			
			List<Integer> trigramSecondSpeechFeatures = generateFeatures(secondSpeech);
			fv = generateFeatureString(trigramSecondSpeechFeatures);
			fv = convert(secondParty) + " " + fv ;
			System.out.println(fv);
			writer.write(fv);
			writer.newLine();
	
			
			
		}
		
		reader.close();
		writer.close();
	}
	
	
	private String convert(String party) 
	{
		// TODO Auto-generated method stub
		if(party.equalsIgnoreCase("d"))
		{
			return "1" ;
		}
		if(party.equalsIgnoreCase("r"))
		{
			return "2" ;
		}
		else
		{
			return null ;
		}
	}

	private String generateFeatureString(
			List<Integer> trigramSpeechFeatures) 
	{
		// TODO Auto-generated method stub
		StringBuffer ret = new StringBuffer();
		for ( Integer trigram : trigramSpeechFeatures)
		{
			ret.append(trigram);
			ret.append(":") ;
			ret.append("1") ;
			ret.append(" ");
			
		}
		return ret.toString().trim();
	}

	private List<Integer> generateFeatures(String firstSpeech) 
	{
		// TODO Auto-generated method stub
		
		List<Integer> trigramFeatures = new ArrayList<Integer>() ;
		
		String words[] = firstSpeech.split("\\s+") ;
		
		for ( int i = 0 ; i < words.length-2 ; i++ )
		{
			String one = words[i].trim();
			String two = words[i+1].trim();
			String three = words[i+2].trim() ;
			
			checkUnigrams(one);
			checkUnigrams(two);
			checkUnigrams(three);
			
			checkBigrams(one,two);
			checkBigrams(two,three);
			checkTrigrams(one,two,three);
					
			int index = trigrams.indexOf(three+"|||"+two+"|||"+one) ;
			
			if(!trigramFeatures.contains(index))
			{
				trigramFeatures.add(index) ;
			}
			
			
		}
		
		java.util.Collections.sort(trigramFeatures) ;
		
		return trigramFeatures;
	}

	private void checkTrigrams(String one, String two, String three) {
		// TODO Auto-generated method stub
		if(!trigrams.contains(three+"|||"+two+"|||"+one))
		{
			trigrams.add(three+"|||"+two+"|||"+one);
		}
	}

	private void checkBigrams(String one, String two) {
		// TODO Auto-generated method stub
		if(!bigrams.contains(two+"|||"+one))
		{
			bigrams.add(two+"|||"+one);
		}
	}

	private void checkUnigrams(String one) 
	{
		// TODO Auto-generated method stub
		if(!unigrams.contains(one))
		{
			unigrams.add(one);
		}
	}

	private List<String> ExtractTopics(String topicString) 
	{
		// TODO Auto-generated method stub
		topicString = topicString.substring(2,topicString.length()-2);
		List<String> topics = new ArrayList<String>(Arrays.asList(topicString.split(","))) ;
		return topics;
	}

	private String ExtractDebateName(String debateName) 
	{
		// TODO Auto-generated method stub
		//./data/convote_v1.1/data_stage_one/mixed_set/426.txt
		
		int index = debateName.lastIndexOf("/");
		String debate = debateName.substring(index+1,debateName.length());
		return debate;
	}

	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		String path = "./data/output/LDA/" ;
		String file = "perspectives_121326_904.txt" ;
		PerspectiveClassification povClassifierObj = new PerspectiveClassification();
		povClassifierObj.loadPerspectiveData(path, file) ;
	}

}
