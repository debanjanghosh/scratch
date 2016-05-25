package com.research.course.debate.MI;

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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import jtextpro.JTextPro;
import jtextpro.JTextProcessor;

public class CalculateMIPerParty {

	/**
	 * @param args
	 */
	private JTextProcessor jtProcObj ;
	private final String JTPModelPath = "./data/models/";
	private Map<String,Integer> corpusMap ;
	private Map<String,Map<String,Integer>> politicsTopicMap ;
	private Map<String,Integer> topicSpeechMap ;
	
	class EachMIFeature implements Comparable
	{
		public String getFeature() {
			return feature;
		}
		public void setFeature(String feature) {
			this.feature = feature;
		}
		public double getMI() {
			return MI;
		}
		public void setMI(double mI) {
			MI = mI;
		}
		public String getTopic() {
			return topic;
		}
		public void setTopic(String topic) {
			this.topic = topic;
		}
		private String feature ;
		private double MI ;
		private String topic ;
		private int global ;
		private int local ;
		
		@Override
		public int compareTo(Object arg0) 
		{
			// TODO Auto-generated method stub
			double miScore1 = this.getMI() ;
			double miScore2 = ((EachMIFeature) arg0).getMI() ;
			
			
			if(miScore1 >= miScore2)
			{
				return 0;
			}
			else  
			{
				return 1 ;
			}
		}
		public void setGlobalCount(Integer globalCount) 
		{
			// TODO Auto-generated method stub
			this.global = globalCount ;
		}
		
		public int getGlobalCount()
		{
			return global ;
		}
		
		public void setLocallCount(Integer localCount) {
			// TODO Auto-generated method stub
			this.local = localCount ;
		}
		
		public int getLocal ()
		{
			return local ;
		}
		
		public String toString()
		{
			return feature +"\t" + global +"\t" + local +"\t" + MI ;
		}
	}
	
	Map<String,List<EachMIFeature>> debateTopicFeatMap ;
	private ArrayList<String> stopWords; 
	
	public CalculateMIPerParty()
	{
		jtProcObj = new JTextProcessor();
	    jtProcObj.setModelPath("./data/models/");
	    jtProcObj.initJTextProObject();
	    
	    corpusMap = new HashMap<String,Integer>();
	    politicsTopicMap = new HashMap<String,Map<String,Integer>>() ;
	    topicSpeechMap = new HashMap<String,Integer>();
	    
	    debateTopicFeatMap = new HashMap<String,List<EachMIFeature>>() ;
	}
	
	public void loadStopWords ( String path ) throws IOException
	{
		stopWords = new ArrayList<String>();
		
		BufferedReader reader = null ;
		reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( path + "stop_words.txt") , "UTF8") );
		
		//header
		String line = reader.readLine();
		while (true )
		{
			line = reader.readLine();
			if ( null == line )
			{
				break ;
			}
			
			stopWords.add(line.trim());
		}
		
		reader.close();
	}
	
	public void loadEachDebateTopics( String path ) throws IOException
	{
		File file = new File ( path );
		String files[] = file.list() ;
		
		BufferedReader reader = null ;
		Map<String,Integer> eachDebateMap = null ;
		for ( String f : files )
		{
			File fl = new File (path + f );
			if (fl.isDirectory())
			{
				continue ;
			}
			if ( f.contains("Store") || f.contains("zip"))
			{
				continue ;
			}
			
			String debate = f.substring(0,f.indexOf(".")) ;
			if ( debate.contains("16"))
			{
				System.out.println("here1");
				
			}
			
			eachDebateMap = new HashMap<String,Integer>();
			
			
			reader = new BufferedReader(new InputStreamReader (
					new FileInputStream( path + f) , "UTF8") );
			
			//header
			String line = reader.readLine();
			int index = 0 ;
			while (true)
			{
				line = reader.readLine();
				if (line == null)
				{
					break;
				}
				
				String features[] = line.split("\t");
				String politics = features[4];
				String speech = features[7];
				
				@SuppressWarnings("unchecked")
				List<String>sentences = jtProcObj.getSentences(speech) ;
				
				for ( String sentence : sentences )
				{
					List<String> tokens = jtProcObj.getTokens(sentence);
				//	List<String> tokens = jtProcObj.getPhrases(sentence);
					
					for ( String token : tokens )
					{
						//global map of stop words
						if(stopWords.contains(token))
						{
							continue ;
						}
						// no number
						if(!checkAlphaNumeric(token))
						{
							continue ;
						}
						// no two letters
						if(token.length() < 3)
						{
							continue ;
						}
						
						Integer old = corpusMap.get(token);
						if ( null == old )
						{
							old = 0 ;
						}
						corpusMap.put(token, old+1);
						
						eachDebateMap = politicsTopicMap.get(politics);
						if ( null == eachDebateMap )
						{
							eachDebateMap = new HashMap<String,Integer>();
						}
						else
						{
							old = eachDebateMap.get(token);
							if ( null == old )
							{
								old = 0 ;
							}
							
						}
						eachDebateMap.put(token, old+1);
						politicsTopicMap.put(politics, eachDebateMap);
					}
					
				}
				index++;
				
			}
			
			reader.close();
			topicSpeechMap.put(debate,index);
			
		}
		
	}
	
	public void calculateMIForEachToken (String path) throws FileNotFoundException, IOException
	{
		int totalFileList = getTotalSpeechList(topicSpeechMap);
		List<EachMIFeature> eachTopicMIList = null ;
		
		for( String politics : politicsTopicMap.keySet() )
		{
			
			int fileCount = 3;//topicSpeechMap.get(politics) ;
			
			Map<String,Integer > eachCatMap = politicsTopicMap.get(politics);

			eachTopicMIList = new ArrayList<EachMIFeature>();
			
			EachMIFeature eachFeat = null ;
			for(String token : eachCatMap.keySet() )
			{
				if (token.contains("milit"))
				{
//					System.out.println("here2");

				}
				
				Integer localCount = eachCatMap.get(token);
				Integer globalCount = corpusMap.get(token);
				if(null == globalCount)
				{
					//should not reach here...
					System.out.println("check the term: "+token);
					continue  ;
				}
				
				double MI = calcMutInfoVal(token , globalCount ,localCount , fileCount , totalFileList );
			//	double tfidf = calcTFIDFIntoVal(token, localCount)
				
				
				eachFeat = new EachMIFeature();
				eachFeat.setTopic(politics);
				eachFeat.setFeature(token);
				eachFeat.setGlobalCount(globalCount);
				eachFeat.setLocallCount(localCount);
				
				eachFeat.setMI(MI);
				

				eachTopicMIList.add(eachFeat);
				
			}
			sortingMI(path, politics, eachTopicMIList);
		}
			//sort the MI value 
		//	sortingMI(catName) ;
		
	}
	
	private boolean checkAlphaNumeric(String token) 
	{
		// TODO Auto-generated method stub
		 char[] chars = token.toCharArray();
		for ( char c : chars )
		{
			if ( 'a' <= c && c<= 'z')
			{
				return true ;
			}
		}
		return false;
	}

	private void sortingMI(String path, String debate, List<EachMIFeature> eachTopicMIList) throws IOException, FileNotFoundException
	{
		// TODO Auto-generated method stub
		java.util.Collections.sort(eachTopicMIList);
		
		Writer writer = new BufferedWriter( new OutputStreamWriter ( new FileOutputStream ( path + "/politics_MI_tokens/"+debate +".MI.txt"), "UTF8") );
		writer.write("topic"+"\t"+"feature"+"\t"+"global_count"+"\t"+"local_count"+"\t"+"MI");
		writer.write("\n");
		
		int i = 0 ;
		for ( EachMIFeature feature : eachTopicMIList)
		{
			writer.write(debate + "\t" + feature.toString());
			writer.write("\n");
			i++ ;
			if ( i == 301)
			{
		//		break ;
			}
		}
		writer.close() ;
		
		
	}

	private int getTotalSpeechList(
			Map<String, Integer> debateCountMap) 
	{
		// TODO Auto-generated method stub
		int totalSpeechCount = 0 ;
		for ( String topic : debateCountMap.keySet() )
		{
			totalSpeechCount += debateCountMap.get(topic);
		}
		
		return totalSpeechCount ;
	}

	public double calcMutInfoVal(String featBuffer , int crpsCount , int catCount , int catfile_size , int featfile_size)
	{
		
		double cat_feature_11 = (double)(catCount)/(double)(featfile_size) ;
		double cat_11 = (double)(catfile_size) / (double)(featfile_size) ; //hardcoded to check acq catgry only...
		double feature_11 = (double)(crpsCount)/(double)(featfile_size);

		
		
		double log_cat_feature_11 = Math.log10 (cat_feature_11) ;
		double log_cat_11 = Math.log10(cat_11) ;
		double log_feature_11 = Math.log10(feature_11) ;
		double MI_11 = (cat_feature_11)*((log_cat_feature_11) - (log_cat_11 + log_feature_11))/(Math.log10(2d)) ;



		double cat_feature_10 = (double)(catfile_size - catCount)/(double)(featfile_size) ;
		double cat_10 = cat_11 ;
		double feature_10 = (double)(featfile_size - crpsCount)/(double)(featfile_size) ;

		double log_cat_feature_10 ;
		
		if(cat_feature_10 <=0d)
			log_cat_feature_10 = 0.0 ;
		else
			log_cat_feature_10 = Math.log10(cat_feature_10) ;

		double log_cat_10 = Math.log10(cat_10) ;
		double log_feature_10 = Math.log10(feature_10) ;
		double MI_10 = (cat_feature_10)* ((log_cat_feature_10) - (log_cat_10 + log_feature_10))/(Math.log10(2d)) ;


		double cat_feature_01 = (double)(crpsCount - catCount)/(double)(featfile_size) ;
		double cat_01 = (double)(featfile_size-catfile_size)/(double)(featfile_size) ;
		double feature_01 = feature_11 ;

		double log_cat_feature_01 ;
		if(cat_feature_01 <= 0d)
			log_cat_feature_01 = 0.0;
		else
			log_cat_feature_01 = Math.log10(cat_feature_01) ;
		
		double log_cat_01 = Math.log10(cat_01) ;
		double log_feature_01 = Math.log10(feature_01) ;
		double MI_01 = (cat_feature_01) * ((log_cat_feature_01) - ( log_cat_01 + log_feature_01))/(Math.log10(2d)) ;

		
		double cat_feature_00 = (double)(featfile_size - (crpsCount + catfile_size) + catCount )/(double)(featfile_size) ;
		double cat_00 = cat_01 ;
		double feature_00 = (double)(featfile_size - crpsCount)/(double)(featfile_size) ;

		double log_cat_feature_00 = Math.log10(cat_feature_00) ;
		double log_cat_00 = Math.log10(cat_00) ;
		double log_feature_00 = Math.log10(feature_00) ;
		double MI_00 = (cat_feature_00) * (( log_cat_feature_00) - (log_cat_00 + log_feature_00 )) /(Math.log10(2d)) ;


		double MI_all = MI_11 + MI_10 + MI_01 + MI_00 ;

		/*
		feat = new CEachFeature() ;

		feat->featName = std::string(featBuffer) ;
		feat->count = crpsCount ;
		feat->dummy = MI_all ;
		feat->checked = false ;

		FeatureVector.push_back(feat);
*/
		return MI_all ;
	
	}
		
		
	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		String path = "data/convote_v1.1/data_stage_one/training_set/topics_together/";
		String config = "data/config/";
		CalculateMIPerParty calcMIObj = new CalculateMIPerParty() ;
		calcMIObj.loadStopWords(config);
		calcMIObj.loadEachDebateTopics(path);
		calcMIObj.calculateMIForEachToken(path);
	}

}
