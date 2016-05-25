package com.research.course.debate.LDA;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.research.course.debate.rsrelations.Pair;
import com.research.course.debate.util.CompareUtils;
import com.research.course.debate.util.DocumentProperty;

public class TopicModelCrimeFeatureGenerator {
	// private Logger logger = LoggerFactory.getLogger( getClass() );

	private String name;
	private String description;

	private static float LDA_TOPIC_THRESH = 15;
	
	private static double LDA_TOPIC_PROB_THRESH = 0.1;
	
	private Map<String,List<String>> userTweetListMap ;
	

	private LDAGenerator ldaGenrObj;
	private HashMap<String, String> topicCategoryMap;
	private HashMap<Pair<Integer, Integer>, Set<String>> topicSpeechPosnDebate;

	public TopicModelCrimeFeatureGenerator() {
		super();
		name = TopicModelCrimeFeatureGenerator.class.getSimpleName();
		description = "Features: LDA topic similarities";

		ldaGenrObj = new LDAGenerator();
		userTweetListMap = new HashMap<String,List<String>>() ;
	}

	public void setOutputDirAndExtn(String outputDir, String extn)
			throws IOException, ClassNotFoundException {
		ldaGenrObj.setOutputPath(outputDir);
		ldaGenrObj.setLDaParameters(extn);
	}

	public TopicModelCrimeFeatureGenerator(String name, String description) {
		this();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, Double> generateFeatures(String firstSpeech,
			String secondSpeech)

	{
		return null;
	}

	private Map<String,Float> convertSpeechToLDAMAp ( String sourceDocument ) throws Exception
	{
		int ldaSize = 10;
		
		String cleanSourceBodyText = sourceDocument;
		SortedSet<LDADataPair> sourceBodyLdaData = getLDAData(cleanSourceBodyText);
		Map<String, Float> sourceLdaMap = convertLdaToMap(sourceBodyLdaData,
				ldaSize);
		
		return sourceLdaMap ;
	}
	
	
	private Map<String, Double> computeNoveltyValues(Map<String,Float> sourceLdaMap,
			Map<String,Float> candidateLdaMap) throws Exception {

		
		// find the cosine similarity - # of new topics in top 10 - highest
		// score in top five new topic
		List<Float> latentScore = computeNoveltyFeature(sourceLdaMap,
				candidateLdaMap);

		Map<String, Double> novels = new HashMap<String, Double>();
		novels.put("COSINE_SIM", (double)latentScore.get(0)) ;
		novels.put("NUM_NOVEL_TOPICS", (double)latentScore.get(1)) ;
		novels.put("MAX_NOVEL_SCORE", (double)latentScore.get(2)) ;
		
		
		return novels;

	}

	private Map<String, Double> computeFeatureValues(String sourceDocument,
			String candidate) throws Exception {

		int ldaSize = 10;

		// source body
		String cleanSourceBodyText = sourceDocument;
		SortedSet<LDADataPair> sourceBodyLdaData = getLDAData(cleanSourceBodyText);
		Map<String, Float> sourceLdaMap = convertLdaToMap(sourceBodyLdaData,
				ldaSize);

		// candidate body
		String cleanCandidateBodyText = candidate;
		SortedSet<LDADataPair> candidateBodyLdaData = getLDAData(cleanCandidateBodyText);
		Map<String, Float> candidateLdaMap = convertLdaToMap(
				candidateBodyLdaData, ldaSize);

		List<Float> latentScore = computeTopicalFeature(sourceLdaMap,
				candidateLdaMap);

		Map<String, Double> feature = new HashMap<String, Double>();

		// logger.debug( sourceDocument.getDocumentId() + "\t" +
		// candidate.getDocumentId() + "\t" +
		// latentScore.get( 0 ) + "\t" + latentScore.get( 1 ) + "\t" +
		// latentScore.get( 2 ) );

		return feature;
	}

	private SortedSet<LDADataPair> getLDAData(String cleanBodyText)
			throws Exception {
		// TODO Auto-generated method stub
		SortedSet<LDADataPair> bodyLdaData = null;

		if (cleanBodyText.length() > 10) 
		{
			bodyLdaData = ldaGenrObj.calculateLda(cleanBodyText);
		} else {
			bodyLdaData = new TreeSet<LDADataPair>();
		}
		return bodyLdaData;
	}

	private Map<String, Float> convertLdaToMap(SortedSet<LDADataPair> ldaData) {
		Map<String, Float> map = new LinkedHashMap<String, Float>(
				ldaData.size());

		for (LDADataPair pair : ldaData) {
			map.put(String.valueOf(pair.getKey()), pair.getScore());
		}
		return map;
	}

	private Map<String, Float> convertLdaToMap(SortedSet<LDADataPair> ldaData,
			int size) {
		Map<String, Float> map = new LinkedHashMap<String, Float>(size);

		int index = 1;
		for (LDADataPair pair : ldaData) {
			map.put(String.valueOf(pair.getKey()), pair.getScore());
			if (index == size) {
				break;
			}
			index++;
		}
		return map;
	}

	private List<Float> computeNoveltyFeature(Map<String, Float> sourceLDAMap,
			Map<String, Float> candidLDAMap) {
		List<Float> latentScore = new ArrayList<Float>();
		float topicsim = 0.0f;
		float topicoverlap = 0.0f;
		float maxtopicsc = 0.0f;

		if (sourceLDAMap == null || sourceLDAMap.isEmpty()
				|| candidLDAMap == null || candidLDAMap.isEmpty()) {
			latentScore.add(topicsim);
			latentScore.add(topicoverlap);
			latentScore.add(maxtopicsc);

			return latentScore;
		}

		//similarity
		topicsim = CompareUtils
				.computeCosSimilarity(sourceLDAMap, candidLDAMap);

		// novelty?
		 //1 - # of new topics in top 10
		// highest value of new topic in top 5

		float maxNovel = 0f;
		float numNovel = 0f ;
		int index = 0 ;
		for (Map.Entry<String, Float> candme : candidLDAMap.entrySet()) 
		{
			String key = candme.getKey();
			Float value = candme.getValue();

			if (value > maxtopicsc) 
			{
				maxtopicsc = value;
			}

			Float srcv = sourceLDAMap.get(key);
			if (srcv != null) 
			{
				topicoverlap++;
			}
			else
			{
				if ( index <10 )
				{
					if ( value > LDA_TOPIC_PROB_THRESH)
					{
						numNovel++ ;
					}
				}
				if ( index < 5 )
				{
					if ( value > maxNovel)
					{
						maxNovel = value ;
					}
				}
			}
			index++ ;
		}

		latentScore.add(topicsim);
		latentScore.add(numNovel);
		latentScore.add(maxNovel);

		return latentScore;
	}
	
	private Set<String> computeOnlyNovelTopics ( Map<String, Float> sourceLDAMap,
			Map<String, Float> candidLDAMap)
	{
		Set<String> novels = new HashSet<String>();
		
		
		for (Map.Entry<String, Float> candme : candidLDAMap.entrySet()) 
		{
			String key = candme.getKey();
			Float value = candme.getValue();

			Float srcv = sourceLDAMap.get(key);
			if (srcv != null) 
			{
				if ( srcv <=LDA_TOPIC_PROB_THRESH)
				{
					//almost not present...
					if ( value >=LDA_TOPIC_PROB_THRESH)
					{
		//				novels.add(key);
					}
				}
			}
			else 
			{
				if ( value >= LDA_TOPIC_PROB_THRESH)
				{
					novels.add(key);
				}
			}

		}

		
		return novels ;
	}

	private Set<String> getCommons(Map<String, Float> sourceLDAMap,
			Map<String, Float> candidLDAMap) 
	{
		// TODO Auto-generated method stub
		Set<String> commonTopics = new HashSet<String>() ;
		for (Map.Entry<String, Float> candme : candidLDAMap.entrySet()) 
		{
			String key = candme.getKey();
			Float value = candme.getValue();

			Float srcv = sourceLDAMap.get(key);
			if (srcv != null) 
			{
				commonTopics.add(key);
			} 
		}
		return commonTopics ;
	}

	private List<Float> computeTopicalFeature(Map<String, Float> sourceLDAMap,
			Map<String, Float> candidLDAMap) {
		List<Float> latentScore = new ArrayList<Float>();
		float topicsim = 0.0f;
		float topicoverlap = 0.0f;
		float maxtopicsc = 0.0f;

		if (sourceLDAMap == null || sourceLDAMap.isEmpty()
				|| candidLDAMap == null || candidLDAMap.isEmpty()) {
			latentScore.add(topicsim);
			latentScore.add(topicoverlap);
			latentScore.add(maxtopicsc);

			return latentScore;
		}

		topicsim = CompareUtils
				.computeCosSimilarity(sourceLDAMap, candidLDAMap);

		// novelty?

		float topCount = 0f;

		for (Map.Entry<String, Float> candme : candidLDAMap.entrySet()) {
			String key = candme.getKey();
			Float value = candme.getValue();

			if (value > maxtopicsc) {
				maxtopicsc = value;
			}

			Float srcv = sourceLDAMap.get(key);
			if (srcv != null) {
				topicoverlap++;
			} else {
				if (value >= 0.09) {
					topCount++;
				}
			}

		}

		latentScore.add(topicsim);
		latentScore.add(topicoverlap);
		latentScore.add(topCount);

		return latentScore;
	}

	public void compareTopicsBetweenEachTwoSpeeches(String inputDir) throws Exception {
		File file = new File(inputDir);
		File files[] = file.listFiles();

		if (files != null) {
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) {
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) {
					continue;
				}

				if (f.toString().contains("282") || f.toString().contains("414")) 
				{
					continue;
				}

				System.out.println(" debate file is " + f);

				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);

				int outer = 1 ;
				
				//for cosine similarity - maintain four sub lists
				//Y-Y/Y-N/N-Y/N-N
				//first for Y-Y
				HashMap<Integer,List<Double>> yyCosineMap = new HashMap<Integer,List<Double>>() ;
				HashMap<Integer,List<Double>> ynCosineMap = new HashMap<Integer,List<Double>>() ;
				HashMap<Integer,List<Double>> nyCosineMap = new HashMap<Integer,List<Double>>() ;
				HashMap<Integer,List<Double>> nnCosineMap = new HashMap<Integer,List<Double>>() ;
				
				List<Double> values = null ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstParty = fullSpeech1.split("\\|")[0];
					String firstVote = fullSpeech1.split("\\|")[1];
					String firstSpeech = fullSpeech1.split("\\|")[2];
					Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
					
					System.out.println(outer +"\t" + fullSpeech1);
					System.out.println(outer +"\t" + speechMap1);

					int inner = 1 ;
					for (int j = i + 1; j < debateSpeeches.size(); j++) 
					{
						String fullSpeech2 = debateSpeeches.get(j) ;
						String secondParty = fullSpeech2.split("\\|")[0];
						String secondVote = fullSpeech2.split("\\|")[1];
						String secondSpeech = fullSpeech2.split("\\|")[2];
						
				//		if ( ( firstParty.equalsIgnoreCase(secondParty) ) )
				//		{
				//			continue ;
				//		}
						
				//		if ( firstParty.equalsIgnoreCase(secondParty) && firstParty.equalsIgnoreCase("R"))
				//		{
				//			continue ;
				//		}
						
						Map<String,Float> speechMap2 = convertSpeechToLDAMAp(secondSpeech) ;
						
						Map<String, Double> noveltyFeatures = computeNoveltyValues(
								speechMap1, speechMap2);

						if ( inner > 5 )
						{
							break ;
						}
						System.out.println(inner + "\t" + fullSpeech2);
						System.out.println(inner +"\t" + speechMap2);
							
						System.out.println(outer + "\t" + inner +"\t" + noveltyFeatures.toString());
							
						double cosine = noveltyFeatures.get("COSINE_SIM") ;
						//yy
						if (  ( firstVote.equalsIgnoreCase(secondVote)  ) && (firstVote.equalsIgnoreCase("Y")) )
						{
							values = yyCosineMap.get(inner);
							if ( null == values )
							{
								values = new ArrayList<Double>();
							}
							
							
							values.add(cosine) ;
							yyCosineMap.put(inner, values) ;
						}
						//nn
						else if (  ( firstVote.equalsIgnoreCase(secondVote)  ) && (firstVote.equalsIgnoreCase("N")) )
						{
							values = nnCosineMap.get(inner);
							if ( null == values )
							{
								values = new ArrayList<Double>();
							}
							
							
							values.add(cosine) ;
							nnCosineMap.put(inner, values) ;
							
						}
						//ny
						else if (  !( firstVote.equalsIgnoreCase(secondVote)  ) && (firstVote.equalsIgnoreCase("N")) )
						{
							values = nyCosineMap.get(inner);
						
							if ( null == values )
							{
								values = new ArrayList<Double>();
							}
							
							
							values.add(cosine) ;
							nyCosineMap.put(inner, values) ;
						}
						//yn
						else if (  !( firstVote.equalsIgnoreCase(secondVote)  ) && (firstVote.equalsIgnoreCase("Y")) )
						{
							values = ynCosineMap.get(inner);
							
							if ( null == values )
							{
								values = new ArrayList<Double>();
							}
							
							
							values.add(cosine) ;
							ynCosineMap.put(inner, values) ;
							
						}
						else
						{
							System.out.println("error in type of votes - check") ;
						}
						
						inner++ ;
					}
					System.out.println();
					outer++ ;
				}

				for (Integer key : yyCosineMap.keySet() )
				{
					values = yyCosineMap.get(key);
					double avg = getMedian(values);
	//				System.out.println("yy" + "\t" + key + "\t" + avg + "\t" + values) ;
				}
				for (Integer key : nnCosineMap.keySet() )
				{
					values = nnCosineMap.get(key);
					double avg = getMedian(values);
	//				System.out.println("nn" +"\t" + key + "\t" + avg + "\t" + values) ;
				}
				for (Integer key : ynCosineMap.keySet() )
				{
					values = ynCosineMap.get(key);
					double avg = getMedian(values);
	//				System.out.println("yn" +"\t" + key + "\t" + avg + "\t" + values) ;
				}
				for (Integer key : nyCosineMap.keySet() )
				{
					values = nyCosineMap.get(key);
					double avg = getMedian(values);
	//				System.out.println("ny" +"\t" + key + "\t" + avg + "\t" + values) ;
				}
				
			}
			
			
			
		}

	}

	private double getMedian(List<Double> values) 
	{
		// TODO Auto-generated method stub
		Double sum = 0d ;
		
		java.util.Collections.sort(values);
		Object[] v = values.toArray() ;
		int middle =v.length/2;
	    if (v.length %2 == 1) {
	        return (Double) v[middle];
	    } else 
	    {
	        sum = (Double)  v[middle-1]  + (Double) v[middle] ;
	        sum = sum/2d ;
	    }
		
		return sum;
	}

	private float getAvg(List<Float> values) 
	{
		// TODO Auto-generated method stub
		float sum = 0f ;
		
		for ( float v : values )
		{
			sum = sum + v ;
		}
		sum = sum/(float)values.size();
		
		return sum ;
	}

	
	public void segmentEachSpeech( String inputDir ) throws Exception 
	{
		File file = new File(inputDir);
		File files[] = file.listFiles();

		BufferedWriter writer = null ;
		
		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);
				
				int index = 1 ;
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstSpeech = fullSpeech1.split("\\|")[2];
					writer = new BufferedWriter ( new OutputStreamWriter (new FileOutputStream
							(inputDir + "/" + "separate_speech/" + f.getName() +"." + index + ".txt" ),"UTF-8") ) ;
				
					writer.write(firstSpeech) ;
					writer.newLine();
					
					index++ ;
				//	i = i + 1 ;
				}	
				writer.close() ;
			}
		}
	}
	
	public void createTopTopicListPerDebate ( String inputDir ) throws Exception
	{
		File file = new File(inputDir);
		File files[] = file.listFiles();

		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}

				if  (f.toString().contains("16") || f.toString().contains("282") )//|| f.toString().contains("16")) 
				{
					continue;
				}

				System.out.println(" debate file is " + f);

				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);

				int index = 1 ;
				
				Map<String,List<Float>> cumulativeTopics = new HashMap<String,List<Float>>() ;
				List<Float> scores = null ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstParty = fullSpeech1.split("\\|")[0];
					String firstVote = fullSpeech1.split("\\|")[1];
					String firstSpeech = fullSpeech1.split("\\|")[2];
				//	i = i + 1 ;
					Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
					
					for ( String key : speechMap1.keySet() )
					{
						float value = speechMap1.get(key);
						if ( value > LDA_TOPIC_PROB_THRESH)
						{
							scores = cumulativeTopics.get(key);
							if ( null == scores )
							{
								scores = new ArrayList<Float>();
							}
							scores.add(value);
							cumulativeTopics.put(key,scores);
						}
					}
					
					
					index++ ;
				}

				
				
				for ( String key : cumulativeTopics.keySet())
				{
					List<Float> values = cumulativeTopics.get(key);
					float avg = getAvg(values);
					
					System.out.println(f.getName() + "\t" + key +"\t" + values.size() + "\t" + avg) ;
				}
				
				System.out.println("total number of lines " + index) ;
			}
			
			
		}

	}
	
	public void compareContineousTopicsBetnSameParty (String inputDir, String party1) throws Exception
	{
		
		File file = new File(inputDir);
		File files[] = file.listFiles();

		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}
/*
				if (f.toString().contains("16") || f.toString().contains("282")) 
				{
					continue;
				}
*/				
				if (!f.toString().contains("414")) 
				{
					continue;
				}

				System.out.println(" debate file is " + f);

				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);

				int index = 1 ;
					
				Set<String> cumulativeTopics = new HashSet<String>() ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstParty = fullSpeech1.split("\\|")[0];
					String firstVote = fullSpeech1.split("\\|")[1];
					String firstSpeech = fullSpeech1.split("\\|")[2];
					
					if(firstSpeech.length() < 400)
					{
						continue ;
					}
					//comparing between only same and a particular party
					
					int j = 0 ;
					for ( j = i+1 ; j <  debateSpeeches.size() ; j++ )
					{	
					
						String fullSpeech2 = debateSpeeches.get(j) ;
						String secondParty = fullSpeech2.split("\\|")[0];
						String secondVote = fullSpeech2.split("\\|")[1];
						String secondSpeech = fullSpeech2.split("\\|")[2];
						
							
						if(secondSpeech.length() < 400)
						{
							
							continue ; 
							
						}
					
						//comparing between only same and a particular party
					//	if(!secondParty.equalsIgnoreCase(party))
					//	{
					//		continue ;
					//	}
						
						//we only want to see when the 2nd person belong to a party
						if(!secondParty.equalsIgnoreCase(party1))
						{
					//		i = j-1 ;
							break ;
						}
						//	System.out.println("length of text speeches are " + firstSpeech.length() + "\t" + 
						//			secondSpeech.length()) ;

										// computing novelty scores
						Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
						Map<String,Float> speechMap2 = convertSpeechToLDAMAp(secondSpeech) ;
					
			//			System.out.println("first" +"\t" + firstSpeech);
			//			System.out.println();
			//			System.out.println("second" +"\t" + secondSpeech);
						
						
						
						for ( String key : speechMap1.keySet() )
						{
							float value = speechMap1.get(key);
							if ( value > LDA_TOPIC_PROB_THRESH)
							{
								cumulativeTopics.add(key);
							}
						}
					
						Set<String> cumulTopics = compareCumulativeAndCurrent(cumulativeTopics,speechMap2) ;
					
						if ( index == 54 )
						{
				//							System.out.println("here") ;
						}
					
						Set<String> noveltyFeatures = computeOnlyNovelTopics(
							speechMap1, speechMap2);
						
						Map<String,Integer> topicCategoryMaps = getTopicCategoryMap(noveltyFeatures) ;
						Set<String> commons = getCommons(speechMap1,speechMap2) ;
					
						String out = (index + "\t" + firstParty + "\t" + secondParty + "\t" + firstVote + "\t" + 
								secondVote +"\t" +  cumulTopics.size() + "\t" + noveltyFeatures.size() +"\t" +
								topicCategoryMaps.toString() + "\t" + commons.size()) ;
					
						//		System.out.println(speechMap1.keySet());
						//		System.out.println(speechMap2.keySet());
						
						
						
						System.out.println(out) ;
					
						for ( String key : speechMap2.keySet() )
						{
							float value = speechMap2.get(key);
							if ( value > LDA_TOPIC_PROB_THRESH)
							{
								cumulativeTopics.add(key);
							}
						}
					
						//		System.out.println(index +"\t" + fullSpeech1);
						//		System.out.println(index + "\t" + fullSpeech2);
						//		System.out.println(index +"\t" + speechMap1);
						//		System.out.println(index +"\t" + speechMap2);
						//		System.out.println(index +"\t" + noveltyFeatures.toString());
						index++ ;
						break ;
					}
					
					i = j-1 ;
				}
			}
		}

	}
	
	
	public void compareContineousSpeechesForCumulativeRecords(String inputDir) throws Exception 
	{
		File file = new File(inputDir);
		File files[] = file.listFiles();

		
		String out = ("index" + "\t" + "firstParth" + "\t" + "secondParty" + "\t" + "firstVote" + "\t" + 
				"secondVote" +"\t" +  "new_cumul_topics" + "\t" + "total_cumu_topics" + "\t" + "new_topics_per_AP") ;
	
		System.out.println(out);
		
		Map<String,Integer> debateTotalTopics = new HashMap<String,Integer>();
		Map<String,Map<Integer,Integer>> debateTopicsPercent = new HashMap<String,Map<Integer,Integer>>();
		Map<Integer,Integer> debatePerPercent = null ;
		
		Set<String> totalFiles = new HashSet<String>() ;
		
		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}

				if (!f.toString().contains("84") )//|| f.toString().contains("414")) 
				{
	//				continue;
				}

				System.out.println(" debate file is " + f);
				totalFiles.add(f.getName()) ;
				
				
				
				debatePerPercent = new HashMap<Integer,Integer>();
				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				
				
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);
				
				List<String> indexList = new ArrayList<String>() ;

				int index = 0 ;
				int totalCumulTopics = 0 ;
					
				Set<String> cumulativeTopics = new HashSet<String>() ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstParty = fullSpeech1.split("\\|")[0];
					String firstVote = fullSpeech1.split("\\|")[1];
					String firstSpeech = fullSpeech1.split("\\|")[2];
					
					if(firstSpeech.length() < 400)
					{
						continue ;
					}
					
					int j = 0 ;
					
					for ( j = i+1 ; j <  debateSpeeches.size() ; j++ )
					{	
						String fullSpeech2 = debateSpeeches.get(j) ;
						String secondParty = fullSpeech2.split("\\|")[0];
						String secondVote = fullSpeech2.split("\\|")[1];
						String secondSpeech = fullSpeech2.split("\\|")[2];
						
						if(secondSpeech.length() < 400)
						{
							continue ;
						}
						
					//	System.out.println("length of text speeches are " + firstSpeech.length() + "\t" + 
					//			secondSpeech.length()) ;
	
							// computing novelty scores
						Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
						Map<String,Float> speechMap2 = convertSpeechToLDAMAp(secondSpeech) ;
						for ( String key : speechMap1.keySet() )
						{
							float value = speechMap1.get(key);
							if ( value > LDA_TOPIC_PROB_THRESH)
							{
								cumulativeTopics.add(key);
							}
						}
					
						if ( index == 0 )
						{
							//we need to enter the very first speech into the system as it will
							//help us in calculating the total number of topics in the debate
							totalCumulTopics = cumulativeTopics.size() ;
							 out = (index + "\t" + "BLANK" + "\t" + firstParty + "\t" + "BLANK" + "\t" + 
									firstVote +"\t" +  cumulativeTopics.size() + "\t" + totalCumulTopics + "\t" + cumulativeTopics.size()) ;
							
							indexList.add(out) ;
						
				//			System.out.println(out) ;
				
						}
						index++ ;
					
						Set<String> novelTopics = compareCumulativeAndCurrent(cumulativeTopics,speechMap2) ;
					
						if ( index == 23 )
						{
				//			System.out.println("here") ;
						}
						
						Set<String> noveltyFeatures = computeOnlyNovelTopics(
								speechMap1, speechMap2);
						
				//		System.out.println(speechMap1.keySet());
				//		System.out.println(speechMap2.keySet());
						
						totalCumulTopics = novelTopics.size() + cumulativeTopics.size() ;
						
						out = (index + "\t" + firstParty + "\t" + secondParty + "\t" + firstVote + "\t" + 
									secondVote +"\t" +  novelTopics.size() + "\t" + totalCumulTopics +"\t" + noveltyFeatures.size()) ;
							
						indexList.add(out) ;
						
				//		System.out.println(out) ;
						
						for ( String key : speechMap2.keySet() )
						{
							float value = speechMap2.get(key);
							if ( value > LDA_TOPIC_PROB_THRESH)
							{
								cumulativeTopics.add(key);
							}
						}
						
						break ;
					}
					
					i = j-1 ;
			//		System.out.println(index +"\t" + fullSpeech1);
			//		System.out.println(index + "\t" + fullSpeech2);
					
			//		System.out.println(index +"\t" + speechMap1);
			//		System.out.println(index +"\t" + speechMap2);
					
						
			//		System.out.println(index +"\t" + noveltyFeatures.toString());
						
						
					
				}

				List<Integer> percentPosn = getPercentSplits(indexList.size()-1);
				
				for ( String out1 : indexList)
				{
					String features[] = out1.split("\t") ;
					int idx = Integer.valueOf(features[0]) ;
					int cumulative = Integer.valueOf(features[6]) ;
					
					for ( int p = 0 ; p < percentPosn.size();p++)
					{
						if (percentPosn.get(p) == idx)
						{
							debatePerPercent.put(p, cumulative) ;
							
						}
					}
				}
						
				debateTotalTopics.put(f.getName(),cumulativeTopics.size());
				debateTopicsPercent.put(f.getName(), debatePerPercent) ;
			}
		}

		//total debate topics
//		for ( String debate : debateTotalTopics.keySet())
//		{
//			System.out.println(debate + "\t" + debateTotalTopics.get(debate));
//		}


//		printTheMatrix(debateTopicsPercent,debateTotalTopics) ;
		printTheMatrix2(debateTopicsPercent,debateTotalTopics) ;
		
		
		
		
		
		//print per pecent 
		//load 50% - 100% for each 10% and count the numbers
		
	/*	
		
		for ( double perc = 10d ; perc < 30d ; perc = perc + 10d)
		{

			int oneVal = 0 ;
			int twoVal = 0 ;
			int threeVal = 0 ;
			int fourVal = 0 ;
			int fiveVal = 0 ;
			int sixVal = 0 ;
			int sevVal = 0 ;
			int eightVal = 0 ;
			int nineVal = 0 ;
			int tenVal = 0 ;
			
			for ( String debate : debateTopicsPercent.keySet())
			{
				debatePerPercent = debateTopicsPercent.get(debate);
				boolean flag = false ;
			
				for ( Integer percent : debatePerPercent.keySet())
				{
					int value = debatePerPercent.get(percent);
					double pValue = 100d * ((double) value / (double) debateTotalTopics.get(debate) ) ;
					System.out.println(debate + "\t" + percent + "\t" + pValue) ;
				
					if ( percent == 0 ) //10%
					{
						if(pValue >=perc)
						{
							if ( !flag)
							{
								oneVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 1 ) //20%
					{
						if(pValue >=perc)
						{
							if ( !flag)
							{
								twoVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 2 ) //30%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								threeVal++ ;
								flag = true ;
							}
					}
					}
					if ( percent == 3 ) //40%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								fourVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 4 ) //50%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								fiveVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 5 ) //60%
					{
						if(pValue >=90d)
						{
							if(!flag)
							{
								sixVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 6 ) //70%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								sevVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 7 ) //80%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								eightVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 8 ) //90%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								nineVal++ ;
								flag = true ;
							}
						}
					}
					if ( percent == 9 ) //100%
					{
						if(pValue >=perc)
						{
							if(!flag)
							{
								tenVal++ ;
								flag = true ;
							}
						}
					}
				}
				
			}
			System.out.println(perc +" "+ oneVal + " " + twoVal + " " + threeVal + " " + fourVal + " " + fiveVal 
					+ " " + sixVal + " " + sevVal + " " + eightVal + " " + nineVal + " " + tenVal) ;


	
		}
		*/
	}
	
	private List<Double> getPercentages ( double total)
	{
		List<Double> perc = new ArrayList<Double>();
		
		double num = 10 ;
		double frac = total/num ;
		
		for ( int i = 1 ; i < 10 ; i++)
		{
			perc.add(frac);
			frac = total/num + frac ;
		}
		
		perc.add(total) ;
		return perc ;
	}
	
	private void printTheMatrix2(
			Map<String, Map<Integer, Integer>> debateTopicsPercent, Map<String, Integer> debateTotalTopics) 
	{
		// TODO Auto-generated method stub
		double[][] topicSpeechAPArray = new double[10][10];
		
		int index = 1 ;
		
		for ( String debate : debateTopicsPercent.keySet())
		{
			Map<Integer,Integer> speechTopicMap = debateTopicsPercent.get(debate);
			//find the numbers that represent each 10 % of debates
			List<Double> percentages = getPercentages((double) debateTotalTopics.get(debate)) ;
			
			int i = 0 ;
			int idx = 0 ;
			boolean end = false ;
			for ( Integer topicNum : speechTopicMap.keySet())
			{
				double value = speechTopicMap.get(topicNum);
			//	double percValue = 100d * ((double) value / (double) debateTotalTopics.get(debate) ) ;
				
				for ( int j = idx ;j < 10 ; j++ )
				{
					Double perc = percentages.get(j) ;
		
					if ( value >= perc)
					{
						double old = topicSpeechAPArray[j][i] ;
						if ( old == 0 )
						{
							topicSpeechAPArray[j][i] = 1 ;
							
							if ( j == 9)
							{
								end = true ;
								break;
							}
							
							if ( j == 8 && i == 3)
							{
								System.out.println(j + "," + i + "\t" + debate);
							}
						}
						else
						{
							if ( old == index )
							{
								continue ;
							}
							else
							{
								topicSpeechAPArray[j][i] = old + 1 ;
								if ( j == 9)
								{
									end = true ;
									break;
								}
							}
						}
					}
					else
					{
						idx = j ;
						break; 
					}
				}
				i++;
				if ( end)
				{
					break;
				}
			}
			
			
			
			index++ ;
			
		}
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			String ret = "\t" ;
			for ( int j = 0 ; j < 10 ; j++ )
			{
	//			ret = ret + topicCumulArray[i][j] + "\t" ; 
	//			ret = ret + topicDiagonalArray[i][j] + "\t" ; 
				ret = ret + topicSpeechAPArray[i][j] + "\t" ; 
			}
			
			ret = ret.trim() ; 
			System.out.println(ret);
		}
		
		
		
	}
	private void printTheMatrix(
			Map<String, Map<Integer, Integer>> debateTopicsPercent, Map<String, Integer> debateTotalTopics) 
	{
		// TODO Auto-generated method stub
		double[][] topicSpeechAPArray = new double[10][10];
		
		List<String> dones = new ArrayList<String>() ;
		
		topicSpeechPosnDebate = new HashMap <Pair<Integer,Integer>,Set<String>>() ;
		Set<String> debates = null ;
		
		for ( String debate : debateTopicsPercent.keySet())
		{
			if ( debate.contains("493"))
			{
	//			System.out.println("here");
			}
			Map<Integer,Integer> speechTopicMap = debateTopicsPercent.get(debate);
			
			Set<Double> dummy = new HashSet<Double>() ;
			//the dummy list plays a nice role - because the way
			//we are creating pValue (%) it might be possible that 
			//two numbers fall on a same bucket. For example, 50 and 55
			//topics are covered by 20 and 30 % of speeches where the number
			//of topics such that 50 and 55 fall on a same bucket. We want 
			//only unique debate! so the dummy will take care of that
			
			
			for ( Integer topicNum : speechTopicMap.keySet())
			{
				double value = speechTopicMap.get(topicNum);
				if ( topicNum == 3 )
				{
	//				System.out.println( debate);
				}
				double percValue = 100d * ((double) value / (double) debateTotalTopics.get(debate) ) ;
				double pValue = inPercentage(percValue);
				
				if ( pValue == 1 && topicNum == 0)
				{
					System.out.println(pValue + "\t" + debate);
					
					dones.add(debate);
				}
				if(dummy.contains(pValue))
				{
					continue ;
				}
				dummy.add(pValue) ;
				
				topicSpeechAPArray[(int) pValue-1][topicNum] = topicSpeechAPArray[(int) pValue-1][topicNum] + 1 ;
				
				Pair pair = new Pair((int)pValue-1,(int)topicNum) ;
		
				debates = topicSpeechPosnDebate.get(pair) ;
				if ( null == debates )
				{
					debates = new HashSet<String>();
				}
				debates.add(debate);
				topicSpeechPosnDebate.put(pair, debates);
			}
			
		}
		
		Set<String> keys = debateTotalTopics.keySet() ;
		for ( String key : keys )
		{
			if(!dones.contains(key))
			{
				System.out.println("check");
			}
		}
		
		for ( int i = 0 ; i < 10 ; i++)
		{
		
			for ( int j = 0 ; j < 10 ; j++ )
			{
				Pair pair = new Pair(i,j);
				
				Set<String> oldTopics = topicSpeechPosnDebate.get(pair) ;
				if ( null == oldTopics )
				{
					System.out.println("no topics for "+ pair.toString());
					continue ;
				}
				
			}
		}
		
		
		//cumulative columns
		double[][] topicCumulArray = new double[10][10] ;
		
		for ( int j = 0 ; j < 10 ; j++)
		{
		
			for ( int i = 0 ; i < 10 ; i++ )
			{
				String ret = "\t" ;
				for ( int k = i ; k < 10 ; k++ )
				{
		//			if ( (k+1) < 10 )
					{
						topicCumulArray[i][j] = topicCumulArray[i][j] + topicSpeechAPArray[k][j] ;
						
						Set<String> topicListTwo = getTopicLists(i,j);
						if ( null == topicListTwo )
						{
							System.out.println("no topics for "+ k + " , " + j);
							topicListTwo = new HashSet<String>();
						}
						
						Pair pair = new Pair(k,j);
						
						Set<String> oldTopics = topicSpeechPosnDebate.get(pair) ;
						if ( null == oldTopics )
						{
							continue ;
						}
						
						oldTopics.addAll(topicListTwo);
						pair = new Pair(i,j);
						
						topicSpeechPosnDebate.put(pair, oldTopics);
						
						
						
					}
				}
			}
		}
		
		//print the topic list
		for ( int i = 0 ; i < 10 ; i++)
		{
		
			for ( int j = 0 ; j < 10 ; j++ )
			{
				Pair pair = new Pair(i,j);
				
				Set<String> oldTopics = topicSpeechPosnDebate.get(pair) ;
				if ( null == oldTopics )
				{
					System.out.println("no topics for "+ pair.toString());
					continue ;
				}
				else
				{
					System.out.println( pair.toString() +"\t" + oldTopics.size());
				}
			}
		}
		
		
		
		for ( int i = 0 ; i < 10 ; i++)
		{
		
			for ( int j = 0 ; j < 10 ; j++ )
			{
				if(topicCumulArray[i][j] == 52)
				{
					break ;
				}
				else
				{
					double diff = 52 - topicCumulArray[i][j] ;
					if ( j == 0 )
					{
						
					}
					else
					{
						Set<String> topicListTwo = getTopicLists(i,j);
						Set<String> topicListOne = getTopicLists(i,j-1);
						
						if ( null == topicListTwo || null == topicListOne )
						{
							System.out.println("here");
							continue ;
						}
						
						Set<String> newTopics = compareLists(topicListOne,topicListTwo);
						if ( newTopics.size() > 0 )
						{
							topicListTwo.addAll(newTopics);
							Pair pair = new Pair(i,j);
							topicSpeechPosnDebate.put(pair, topicListTwo);
						}
						
					}
				}
			
			}
		}
		
		
		
/*		
		//most complex part 
		//diagonal values!
		double[][] topicDiagonalArray = new double[10][10] ;
		
		//copy values
		for ( int i = 0 ; i < 10 ; i++)
		{
		
			for ( int j = 0 ; j < 10 ; j++ )
			{
				topicDiagonalArray[i][j] = topicCumulArray[i][j];
			}
		}
		
		
		for ( int j = 0 ; j < 10 ; j++)
		{
		
			for ( int i = 1 ; i < 10 ; i++ )
			{
				
				for ( int k = j+1 ; k < 10 ; k++ )
				{
					if ( topicDiagonalArray[i-1][k] >= topicDiagonalArray[i][j])
					{
		//				topicCumulArray[i][j] = topicCumulArray[i][j] + topicSpeechAPArray[k][j] ;
					}
					else
					{
						topicDiagonalArray[i-1][k] = topicDiagonalArray[i][j] ;
						
						Set<String> topicListOne = getTopicLists(i-1,k);
						Set<String> topicListTwo = getTopicLists(i,j);
						if ( null == topicListTwo  || null == topicListOne)
						{
							continue ;
						}
						
						Pair pair = new Pair(i-1,k);
						topicListOne.addAll(topicListTwo);
				//		topicSpeechPosnDebate.put(pair, topicListOne);
						
					}
				}
			}
		}
		
		//almost done - now the final check of "debates"
		for ( int j = 0 ; j < 10 ; j++)
		{
		
			for ( int i = 1 ; i < 10 ; i++ )
			{
				
				for ( int k = j+1 ; k < 10 ; k++ )
				{
					
					//topic comparisons
					Set<String> topicListOne = getTopicLists(i,j);
					Set<String> topicListTwo = getTopicLists(i-1,k);
					
					if ( null == topicListOne || null == topicListTwo )
					{
						System.out.println("here");
						continue ;
					}
					
					int newTopics = compareLists(topicListOne,topicListTwo);
					if ( newTopics > 0)
					{
						System.out.println("here");
					}
			//		topicDiagonalArray[i-1][k] = topicDiagonalArray[i-1][k] + newTopics ;
				}
			}
		}
		
	*/	
		
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			String ret = "\t" ;
			for ( int j = 0 ; j < 10 ; j++ )
			{
				ret = ret + topicCumulArray[i][j] + "\t" ; 
	//			ret = ret + topicDiagonalArray[i][j] + "\t" ; 
	//			ret = ret + topicSpeechAPArray[i][j] + "\t" ; 
			}
			
			ret = ret.trim() ; 
			System.out.println(ret);
		}
		
		
	}

	private Set<String> compareLists(Set<String> topicListOne,
			Set<String> topicListTwo) 
	{
		// TODO Auto-generated method stub
		int count = 0 ;
		Set<String> newTopics = new HashSet<String>() ;
		for ( String topic : topicListOne )
		{
			if(!topicListTwo.contains(topic))
			{
				count++ ;
			}
		}
		return newTopics;
	}

	private Set<String> getTopicLists(int i, int j) 
	{
		// TODO Auto-generated method stub
		Pair pair = new Pair(i,j);
		return topicSpeechPosnDebate.get(pair);
		
	}

	private double inPercentage(double pValue) 
	{
		// TODO Auto-generated method stub
		//return the closest number between 0 - 10
		pValue = pValue/10d ;
		double minimum = 100 ;
		int perc = 0 ;
		for ( int i = 1 ; i <= 10 ; i++ )
		{
		
			double closeness = Math.abs(i-pValue) ;
			if ( minimum > closeness )
			{
				minimum = closeness ;
				perc = i ;
			}
		}
		
		return perc;
	}

	public void checkParticularTopics ( String inputDir ) throws Exception
	{
		String [] procedures = {"17","31", "35", "18", "84" };
		List<String> proceduralTopics = Arrays.asList(procedures);
		
		String [] actions = {"19"} ;// {"0", "16", "21", "36", "56" ,"53", "78", "80", "19" };
		
	
		List<String> actionTopics = Arrays.asList(actions);
		
		String [] legislate = {"8", "22", "23", "50","51","61","73","83"} ;
		List<String> legislateTopics = Arrays.asList(legislate);
		
		String[] rhetoric = {"15", "14", "64"} ;
		List<String> rhetoricTopics = Arrays.asList(rhetoric);
		
		String[] thematic = {"74"} ;
		List<String> thematicTopics = Arrays.asList(thematic);
		
		
		File file = new File(inputDir);
		File files[] = file.listFiles();

		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}

				if (!f.toString().contains("16"))// || f.toString().contains("282")) 
				{
					continue;
				}

				System.out.println(" debate file is " + f);

				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				
				
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);
				
				List<String> indexList = new ArrayList<String>() ;

				int index = 0 ;
					
				Set<String> cumulativeTopics = new HashSet<String>() ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstParty = fullSpeech1.split("\\|")[0];
					String firstVote = fullSpeech1.split("\\|")[1];
					String firstSpeech = fullSpeech1.split("\\|")[2];
					
					if(firstSpeech.length() < 400)
					{
						continue ;
					}
					
					Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
					for ( String key : speechMap1.keySet() )
					{
						float value = speechMap1.get(key);
						cumulativeTopics.add(key);
						
						if ( value < 0.2)
						{
							continue ;
						}
						
					//	if(proceduralTopics.contains(key) ) 
						if( thematicTopics.contains(key) )//|| 
					//	if (rhetoricTopics.contains(key))
						{
							System.out.println(f.getName()  + "\t" + key +"\t" + value  + "\t" + fullSpeech1);
						}
						
					}
				
					
				}
			}
		}
		
	}
	
	public void loadTopicCategories ( String file ) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( file) , "UTF8") );
		
		topicCategoryMap = new HashMap<String,String>() ;
		
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
			String topic = features[0].trim();
			String category = features[1].trim();
			
			topicCategoryMap.put(topic, category) ;
			
		}
		
		reader.close();
	}
	
	public void compareContineousTopicsForDigression(String inputDir ) throws Exception
	{
		List<Integer> novelTopicHist = new ArrayList<Integer>(10) ;
		List<Integer> commonTopicHist = new ArrayList<Integer>(10) ;
		
		Map<String,Integer> novelTopicTypeMap = new HashMap <String,Integer> ( ) ;
		
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			novelTopicHist.add(0);
		}
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			commonTopicHist.add(0);
		}
		
		File file = new File(inputDir);
		File files[] = file.listFiles();
		
		int totalSpeechNum = 0 ;

		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}

		//		if (f.toString().contains("414") || f.toString().contains("16")) 
		//		{
		//			continue;
		//		}
				
				if (!f.toString().contains("204") ) 
				{
			//		continue;
				}

				System.out.println(" debate file is " + f);
				
				//for each topic (content) check how many speeches have them
				//first check if any topic is singular in a debate in terms of the speeches
				//we cannot compare pair of speeches as this will add copies - lets run only single ones
				
				Map<String,Integer> topicCountMap = new HashMap <String,Integer> ( ) ;
				
			

				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				
				
				Map<String,Map<String,Integer>> speakerTopicMap = new HashMap<String,Map<String,Integer>>() ;
				
			//	List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotesAndReps(f);
		
				List<String> indexList = new ArrayList<String>() ;

				int index = 0 ;
					
				Set<String> cumulativeTopics = new HashSet<String>() ;
				List<String> topicsp = null ;
				
				Map<String,Integer> topicMapPerSpeaker = null ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstSpeaker = fullSpeech1.split("\\|")[0];
					String firstParty = fullSpeech1.split("\\|")[1];
					String firstVote = fullSpeech1.split("\\|")[2];
					String firstSpeech = fullSpeech1.split("\\|")[3];
					
					
				//	firstSpeech = "mr. chairman , i yield myself such time as i may consume . mr. chairman ," +
				//			" i would like to announce that i am terribly flattered by the extravagant things that have been said , but i must confess i did not name this bill after myself . while i deeply appreciate the honor , i am a trifle embarrassed , not thoroughly embarrassed , but a trifle . mr. chairman , most informed people agree that the u.n . is in desperate need of reform . corruption is rampant , as evidenced by the ever-expanding oil-for-food scandal . u.n . peacekeepers have sexually abused children in bosnia , the congo , sierra leone and other places ; and the culture of concealment makes rudimentary oversight virtually impossible . a casual attitude towards conflict-of-interest rules undermines trust in the u.n. 's basic governance . i could spend many hours reciting a litany of waste , fraud , and abuse that has become intolerable . so what do we do about it ? what leverage do we have to bring about change in how this institution operates ? first of all , we pay 22 percent of the budget . that is $ 440 million . we pay 27 percent of the peacekeeping budget . do not ask me what that is . you can not find out . that is a secret . china pays 2.1 percent , or $ 36.5 million . russia pays 1.1 percent , or $ 19 million . over the years , as we listened to the counsels for patience , the u.n. 's failings have grown worse , not lessened . our many warnings , plans and urgings have largely come and gone , with few lasting accomplishments to mark their presence . trust in gradual change has been interpreted as indifference , a very expensive indifference . so the time has finally come when we must in good conscience say `` enough. '' `` enough '' to allowing odious regimes such as cuba , sudan and zimbabwe to masquerade as arbiters of human rights . `` enough '' to peacekeepers exploiting and abusing the people they were sent to protect . `` enough '' to unkept promises and squandering the dreams of generations . very few are opposed to the u.n. 's role in facilitating diplomacy , mediating disputes , monitoring the peace , and feeding the hungry . but we are opposed to the legendary bureaucratization , to political grandstanding , to billions of dollars spent on multitudes of programs with meager results , to the outright misappropriation of funds represented by the oil-for-food program . and we rightly bristle at the gratuitous anti-americanism that has become ingrained over decades , even as our checks continue to be regularly cashed . no observer , be he a passionate supporter of this legislation or dismissive critic , can pretend that the current structure and operations of the u.n . represent an acceptable standard . even the u.n . itself has acknowledged the need for extensive measures and , to its credit , has put forward a number of useful proposals for consideration . in the united states , the recognition of need for change is widely shared and bipartisan . republican and democratic administrations alike have long called for a more focused and accountable budget , one that reflects what should be the true priorities of the organization , shorn of duplicative , ineffective , and outdated programs . members on both sides of the aisle in congress agree that the time has come for far-reaching reform . i have heard no arguments in favor of maintaining the status quo . even the opponents of this legislation concede the need for deep change . the key difference , the all-important difference , between their proposals and the one we have put forward lies in the methods to be used to accomplish that universally desired goal . we are already experiencing strenuous resistance to change from many sources , both within the u.n . and without . but admonishment will not transform sinners into saints ; resolutions of disapproval will not be read ; flexible deadlines and gentle proddings will be ignored . instead , more persuasive measures are called for . this legislation brings to bear instruments of leverage sufficient to the task , the most important being tying the u.s. financial contribution to a series of readily understandable benchmarks . in an effort to derail this legislation , it has been proposed that we hand to the secretary of state the power to selectively withhold funds from the u.n . as a means of inspiring a cooperative attitude in the organization . i certainly mean no disrespect for the current secretary , whom i hold in the highest esteem , but the power of the purse belongs to congress and is not delegable , no matter who holds that high office . we can not escape this burden . the task we face is an extensive one , and i have no illusions regarding the difficulties and the challenges we face . but the choice is simple : we can either seek to accomplish concrete improvements , which will require an enforcement mechanism more credible and more decisive than mere wishes , or we can pretend to do so . for there can be no doubt that any proposal resting upon discretionary decisions concedes in advance that any reform will be fragmentary at best , if there is any at all . we are in a peculiar situation . opponents of change cloak themselves in the robes of defenders of the u.n. , when it is in fact they who would condemn it to irrelevance . those of us who believe the u.n . can yet reclaim its mission and assume the role foreseen by the vision of its founders have no choice but to take up this task of u.n . reform . yes , this is radical surgery . sometimes it is the only way to save the patient . mr. chairman , i reserve the balance of my time . ";
					
				//	firstSpeech = "mr. chairman , i yield myself such time as i may consume . mr. chairman , i rise in strong opposition to this bill , and i urge all of my colleagues across the aisle to do so . let me state at the outset that i fully share the passionate commitment of the gentleman from illinois ( chairman hyde ) to meaningful and thorough reforms at the united nations . this global institution must become more transparent and open , its employees must be held to the highest ethical and moral standards , and the abuses of the oil-for-food program must never be repeated . mr. chairman , the united nations must put an end to its persistent and pathological persecution of the democratic nation of israel , which has become the whipping boy for totalitarian regimes around the globe . serial human rights abusers , mr. chairman , must also be kept off u.n . institutions explicitly designed to fight for the cause of human rights and democracy . mr. chairman , the crushing flow of stories of scandal at the united nations has forced a long-overdue recognition of an essential fact about the place : it is not a real country , like japan or norway . it is a derivative reality reflecting its less-than-perfect member states in a deeply flawed world . i would like to remind my colleagues that there will be no quick fix for an organization composed of 191 member states which , in varying degrees , have their own shortcomings , their own injustices , their own flaws , their own hypocrisies of all types . because a quick fix is not to be expected , and rigid , punitive measures will not bring about a long-term fix , mr. chairman , i must oppose the legislation before the house today and indicate my intention to offer a substitute amendment . just yesterday , mr. chairman , our republican administration informed congress that it strongly opposes the automatic withholding provisions of the hyde bill as well as its infringements upon the president 's constitutional powers . let me repeat that , mr. chairman , and i want my republican friends to listen . the republican administration strongly opposes the hyde bill . this does not come as a surprise to us , mr. chairman . just a few weeks ago , high-ranking officials at the department of state told congress that the legislation would undoubtedly create new arrears at the united nations because not all of the u.n . reform benchmarks contained in the bill are achievable . while many of the reforms being sought in the hyde bill are worthy goals , many require unanimous agreement by all 191 u.n . member states , including the likes of iran , syria , and sudan . mr. chairman , the lord gave us ten commandments , but the bill before the house today gives us 39 . what is worse , mr. chairman , is that if the united nations achieves 38 of these benchmarks and only accomplishes half of the thirty-ninth , the hyde bill automatically , automatically , cuts off 50 percent of the u.s. contribution to the united nations . with this rigid and inflexible mechanism , the legislation before us will undercut , not strengthen , our ability to press for the very reforms we all seek . senior state department officials argue that the bill , if enacted , would severely undermine america 's national security interests by killing desperately needed u.n . peacekeeping operations , including a possible mission to deal with genocide . the state department is not alone in opposing the hyde bill . eight former united states ambassadors to the united nations have expressed their strongest opposition to the bill . these ambassadors include distinguished republicans like jeane kirkpatrick , john danforth , a former distinguished republican senator ; and ambassadors richard holbrooke , madeleine albright , donald mchenry , thomas pickering , bill richardson , and andrew young . they argue that the bill `` threatens to undermine our leadership and effectiveness at the u.n . and the reform effort itself. '' in short , mr. chairman , while the hyde bill has the best of intentions , it will cause our nation to go back into an arrears at the united nations without achieving the desired outcomes . given the important role the united nations is currently playing in afghanistan , in iraq , in darfur , and scores of other places , i fail to see how going into debt at the united nations will promote our national security interests . it will only force the united states to take on greater global responsibilities at the very moment when our troops and our diplomats are already spread thin . i also fail to see , mr. chairman , how tying the hands of our distinguished secretary of state , dr . condoleezza rice , as she pursues reform at the united nations would serve our national interest . the legislation before the congress micromanages every possible reform at the united nations . it creates mechanical , arbitrary , and automatic withholdings , and it gives secretary of state rice zero flexibility to get the job done . for these reasons , mr. chairman , i will offer a substitute amendment to achieve u.n . reform which will give secretary rice the flexibility she asks for , she needs , and she fully deserves from the congress . mr. chairman , i urge all of my colleagues to side with our nation 's bipartisan foreign policy leaders in opposing this bill . mr. chairman , i reserve the balance of my time . ";
					
			//		firstSpeech = "mr. speaker , i rise in support of h. con . res. 36 . once again , activist judges threaten our authority , first of all , to direct federal fund spending ; and , second of all , they attempt to create law . we have required here in congress at universities that receive federal dollars to extend access to military recruiters equal to other outside groups . but in the name of free speech and association , some schools seek to deny their students access to recruiters and rotc , obviously afraid that their students would maybe even make a wrong choice . it is ironic that an institution whose sole function , whole reason for being , is based on the free exchange of ideas , would then boycott the armed forces , the very people who actively protect their academic freedom . it is further ironic that those who are often noted for concern that low-income americans are serving in disproportionate numbers in the armed forces would block many of their students born with a silver spoon access to rotc . my own son currently serves in iraq . he graduated near the top of his class from the u.s. naval academy ; and , last sunday , he had the satisfaction of witnessing the birth of freedom in a land where for 50 years freedom has been an exotic concept . by passing h. con . res. 36 , we reassert our support for freedom and our disdain for those liberal , elite institutions that seek to sensor choices for their wealthy clientele . " ;
					
					if(firstSpeech.length() < 400)
					{
						continue ;
					}
					
					totalSpeechNum++ ;
					
					if(!firstSpeaker.equalsIgnoreCase("400104"))
					{
				//		continue ;
					}
					
					Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
						
					for ( String key : speechMap1.keySet() )
					{
						float value = speechMap1.get(key);
						//for digression feature
						if ( value > LDA_TOPIC_PROB_THRESH)
						{
							String type = topicCategoryMap.get(key);
							if ( type.equalsIgnoreCase("content"))
							{
								Integer old = topicCountMap.get(key);
								if ( null == old )
								{
									old = 0 ;
								}
							
								topicCountMap.put(key, old+1) ;
						//		if(key.equalsIgnoreCase("7") || key.equalsIgnoreCase("62"))
						//		{
						//			System.out.println("here") ;
						//		}
								
								//which speaker?
								
								topicMapPerSpeaker = speakerTopicMap.get(firstSpeaker) ;
								if ( null == topicMapPerSpeaker )
								{
									topicMapPerSpeaker = new HashMap<String,Integer>() ;
								}
								else
								{
									old = topicMapPerSpeaker.get(key);
									if ( null == old )
									{
										old = 0 ;
									}
									topicMapPerSpeaker.put(key, old+1);
								}
								speakerTopicMap.put(firstSpeaker, topicMapPerSpeaker);
							}	
						}
				
						index++ ;
						
					}
					
				}
				
				//check if a speaker brought any topic that not covered by anyone? 
				for ( String speaker : speakerTopicMap.keySet() )
				{
					Map<String,Integer> perSpeakerMap = speakerTopicMap.get(speaker) ;
					Set<String> novelAgendas  = checkAgenda(perSpeakerMap,speakerTopicMap,speaker) ;
					if (!novelAgendas.isEmpty())
					{
		//				System.out.println(f.toString() + "\t" + speaker + "\t" + novelAgendas + "\t" + perSpeakerMap);
					}
				}
				
				for ( String topic :topicCountMap.keySet())
				{
					int count = topicCountMap.get(topic);
					if ( count == 1)
					{
			//			System.out.println(f.toString() + "\t" + topic + "\t" + count);
					}
				}
				
			}
		}	
		
		System.out.println("total speech number " + totalSpeechNum) ;
	}
	
	
	
	private Set<String> checkAgenda(Map<String, Integer> perSpeakerMap1,
			Map<String, Map<String, Integer>> speakerTopicMap, String self) 
	{
		// TODO Auto-generated method stub
		Set<String> perSpeakerSet1 = perSpeakerMap1.keySet() ;
		Set<String> absents = new HashSet<String>() ;
		
		for ( String topic : perSpeakerSet1)
		{
			boolean presence = true ;
			for ( String speaker : speakerTopicMap.keySet())
			{
				if(speaker.equalsIgnoreCase(self))
				{
					continue ;
				}
				
				Map<String, Integer> perSpeakerMap2 = speakerTopicMap.get(speaker) ;
				Set<String> perSpeakerSet2 = perSpeakerMap2.keySet() ;
			
				if (!perSpeakerSet2.contains(topic) )
				{
					presence = false ;
				}
				else
				{
					presence = true ;
					break;
				}
			}
			
		
			if(!presence && perSpeakerMap1.get(topic)>1)
			{
				absents.add(topic) ;
			}
		}
		
		return absents;
	}
	
	public void loadCrimeContent ( String file ) throws IOException
	{
		String path = "./data/crime/input/" ;
		BufferedReader reader = new BufferedReader ( new FileReader (path + file)) ;
		List<String> tweets = null ;
		while (true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			String features[] = line.split("\t") ;
			String user = features[2] ;
			String tweet = features[3] ;
			
			tweets = userTweetListMap.get(user) ;
			if ( null == tweets )
			{
				tweets = new ArrayList<String>() ;
			}
			tweets.add(tweet);
			userTweetListMap.put(user,tweets) ;
		}
		
		reader.close() ;
	}

	
	public void findTopicsBasedOnTopRetweets ( String file  ) throws Exception 
	{
		
		List<String> tweets = new ArrayList<String>() ;
		BufferedReader reader = new BufferedReader ( new FileReader ( "./data/crime/input/top_tweets/" + file)) ;
		
		String header = reader.readLine() ;
		while (true)
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			String features[] = line.split(",") ;
			tweets.add(features[3]);
			
		}
		reader.close() ;
		for ( String tweet : tweets )
		{
			Map<String,Float> speechMap1 = convertSpeechToLDAMAp(tweet) ;
		//	System.out.println(tweet + "\t" + speechMap1.toString()) ;
				
				for ( String key : speechMap1.keySet() )
				{
					float value = speechMap1.get(key);
					
					
					if ( value > LDA_TOPIC_PROB_THRESH )
					{
						System.out.println(key + "\t" + value ) ;
					}
				}
				
				
			
		}
	}

	public void findTopicsBasedOnUsers ( String file ,List<String> users ) throws Exception 
	{
		loadCrimeContent(file) ;
		
		for ( String user : users )
		{
			List<String> tweets = getTweetsForUser(user) ;
			if ( null == tweets )
			{
				System.out.println("no tweets from the user? check " + user) ;
				continue ;
			}
			
			for ( String tweet : tweets )
			{
				Map<String,Float> speechMap1 = convertSpeechToLDAMAp(tweet) ;
				System.out.println(user + "\t" + tweet + "\t" + speechMap1.toString()) ;
			/*	
				for ( String key : speechMap1.keySet() )
				{
					float value = speechMap1.get(key);
					
					
					if ( value > LDA_TOPIC_PROB_THRESH )
					{
						System.out.println(key + "\t" + value ) ;
					}
				}
				*/
				
			}
		}
	}
	
	public List<String> getTweetsForUser ( String user )
	{
		List<String> tweets = userTweetListMap.get(user);
		return tweets ;
	}
	
	public void compareContineousTopics(String inputDir) throws Exception 
	{
		
		//we need to define a list on # of novel topics (histogram)
		List<Integer> novelTopicHist = new ArrayList<Integer>(10) ;
		List<Integer> commonTopicHist = new ArrayList<Integer>(10) ;
		
		Map<String,Integer> novelTopicTypeMap = new HashMap <String,Integer> ( ) ;
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			novelTopicHist.add(0);
		}
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			commonTopicHist.add(0);
		}
		
		File file = new File(inputDir);
		File files[] = file.listFiles();

		String out1 = ("index" + "\t" + "BLANK" + "\t" + "Party" + "\t" + "BLANK" + "\t" + 
				"Vote" +"\t" +  "cumul topics" + "\t" + "novel topics" + "\t" + "topic-category" + "\t" +  "common") ;
		
		System.out.println(out1);
		
		Map<String,Map<String, Double>> debateListWithTopicType = new HashMap<String,Map<String, Double>>() ;
		
		if (files != null) 
		{
			for (File f : files) 
			{

				// discard the Ds_Store and other zip files
				Set<String> allContentTopics = new HashSet<String>() ;
				if (f.isDirectory()) 
				{
					continue;
				}

				if (f.toString().contains("zip")
						|| f.toString().contains("Store")) 
				{
					continue;
				}

		//		if (! (f.toString().contains("414") || f.toString().contains("16")) )
		//		{
	//				continue;
	//			}
				
				if (!f.toString().contains("414.txt") ) 
				{
					continue;
				}

				System.out.println(" debate file is " + f);

				//not only speeches - we get the vote and the party
				//we simply parse to get the text
				
				//for each debate maintain one map
				Map<String,Double> topicTypeEachMap = new HashMap<String,Double> () ;
				
				Map<String,List<String>> speakerTopicMap = new HashMap<String,List<String>>() ;
				
			//	List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotes(f);
				List<String> debateSpeeches = LDADataUtil.getDebateSpeechesWithPartyVotesAndReps(f);
				
				
				
				List<String> indexList = new ArrayList<String>() ;

				int index = 0 ;
					
				Set<String> cumulativeTopics = new HashSet<String>() ;
				List<String> topicsp = null ;
				
				for (int i = 0; i < debateSpeeches.size(); i++) 
				{
					String fullSpeech1 = debateSpeeches.get(i) ;
					String firstSpeaker = fullSpeech1.split("\\|")[0];
					String firstParty = fullSpeech1.split("\\|")[1];
					String firstVote = fullSpeech1.split("\\|")[2];
					String firstSpeech = fullSpeech1.split("\\|")[3];
					
					if(firstSpeech.length() < 400)
					{
						continue ;
					}
					
					int j = 0 ;
					
					for ( j = i+1 ; j <  debateSpeeches.size() ; j++ )
					{
					
						String fullSpeech2 = debateSpeeches.get(j) ;
						String secondSpeaker = fullSpeech2.split("\\|")[0];
						String secondParty = fullSpeech2.split("\\|")[1];
						String secondVote = fullSpeech2.split("\\|")[2];
						String secondSpeech = fullSpeech2.split("\\|")[3];
					
						if(secondSpeech.length() < 400)
						{
							continue ;
						}
						
				//	System.out.println("length of text speeches are " + firstSpeech.length() + "\t" + 
				//			secondSpeech.length()) ;

						// computing novelty scores
						Map<String,Float> speechMap1 = convertSpeechToLDAMAp(firstSpeech) ;
						Map<String,Float> speechMap2 = convertSpeechToLDAMAp(secondSpeech) ;
						
						//filter on the type (content) here
			//			 speechMap1 = filterOnTopics(speechMap1);
			//			 speechMap2 = filterOnTopics(speechMap2);
							
						
						
						for ( String key : speechMap1.keySet() )
						{
							float value = speechMap1.get(key);
							
							
							if ( value > LDA_TOPIC_PROB_THRESH )
							{
								cumulativeTopics.add(key);
							}
							
														
						}
					
						if ( index == 0 )
						{
							//we need to enter the very first speech into the system as it will
							//help us in calculating the total number of topics in the debate
							String out = (index + "\t" + "BLANK" + "\t" + firstParty + "\t" + "BLANK" + "\t" + 
								firstVote +"\t" +  cumulativeTopics.size() + "\t" + cumulativeTopics.size() + "\t" + "0" +"\t" + "0") ;
						
							indexList.add(out) ;
						}
					
						index++ ;
						Set<String> cumulNovelTopics = compareCumulativeAndCurrent(cumulativeTopics,speechMap2) ;
						Set<String> noveltyFeatures = computeOnlyNovelTopics(speechMap1, speechMap2);
						int noveltyLength = noveltyFeatures.size();
						Map<String,Integer> topicCategoryMaps = getTopicCategoryMap(noveltyFeatures) ;
						
						for ( String key : topicCategoryMaps.keySet())
						{
							Integer old = novelTopicTypeMap.get(key);
							Integer val = topicCategoryMaps.get(key);
							if ( null == old )
							{
								old = 0 ;
							}
							novelTopicTypeMap.put(key, old+val);
							
						}
					
						Set<String> commons = getCommons(speechMap1,speechMap2) ;
						int commonLength = commons.size() ;
					
				//		if (  index == 92 )
				//		{
					//			System.out.println(index + "\t" + fullSpeech1) ;
							//	System.out.println(speechMap1);
							//	System.out.println(index + "\t" + fullSpeech2) ;
							//	System.out.println(speechMap2);
							//	System.out.println(noveltyFeatures + "\t"
							//			+ topicCategoryMaps.toString() + "\t" + commons) ;
								
					//	}
						
						topicsp = speakerTopicMap.get(secondSpeaker);
						if ( null == topicsp)
						{
							topicsp = new ArrayList<String>() ;
						}
						topicsp.add(topicCategoryMaps.toString());
						speakerTopicMap.put(secondSpeaker, topicsp);
						
						String out = (index + "\t" + firstParty + "\t" + secondParty + "\t" + firstVote + "\t" + 
								secondVote +"\t" +  cumulNovelTopics.size() + "\t" + noveltyFeatures.size() +"\t" +
								topicCategoryMaps.toString() + "\t" + commons.size()) ;
						
						
						//update the topicTypeEachMap
						for ( String topic : topicCategoryMaps.keySet())
						{
							int value = topicCategoryMaps.get(topic);
							
							String type = topic.substring(0,topic.indexOf("_"));
							
							Double old = topicTypeEachMap.get(type);
							if ( old == null )
							{
								old = (double) 0 ;
							}
							topicTypeEachMap.put(type, old+value) ;
						}
						
						
						indexList.add(out) ;
						
						Set<String> contentTopics = getContentTopics(topicCategoryMaps);
						allContentTopics.addAll(contentTopics) ;

						if ( noveltyLength > 0)
						{
							int old = novelTopicHist.get(noveltyLength-1);
							novelTopicHist.set(noveltyLength-1, old+1);
						}
				
						
						if ( commonLength > 0)
						{
							int old = commonTopicHist.get(commonLength-1);
							commonTopicHist.set(commonLength-1, old+1);
						}
						
						
						
						
						for ( String key : speechMap2.keySet() )
						{
							float value = speechMap2.get(key);
							
							if ( value > LDA_TOPIC_PROB_THRESH )
						
						//	if ( value > LDA_TOPIC_PROB_THRESH)
							{
								cumulativeTopics.add(key);
							}
						}
						
						break ;
					}
					
					i = j-1 ;
					
				}
				
				debateListWithTopicType.put(f.getName(),topicTypeEachMap) ;
				
				List<Integer> percentPosn = getPercentSplits(indexList.size());
				
				for ( String out : indexList)
				{
					String features[] = out.split("\t") ;
					int i = Integer.valueOf(features[0]) ;
					
			//		if (percentPosn.contains(i))
					{
						System.out.println(out) ;
					}
				}
				
				
				for ( String speaker : speakerTopicMap.keySet() )
				{
		//			System.out.println(speaker + "\t" + speakerTopicMap.get(speaker));
				}
				
			//	for ( String topic : allContentTopics)
			//	{
				//	System.out.println(topic) ;
				//}
			}
		}
/*
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			System.out.println("novel " + i + "\t" + novelTopicHist.get(i));
		}
		
		for ( int i = 0 ; i < 10 ; i++ )
		{
			System.out.println("common " + "i" + "\t" + commonTopicHist.get(i));
		}
		
		for ( String key : novelTopicTypeMap.keySet())
		{
			System.out.println(key + "\t" + novelTopicTypeMap.get(key));
		}
*/		
		
		//now print each debate topic list with % 
		String topicTypes[] = {"content" , "legislate", "rhetoric" , "action", "procedure" , "non discernible"} ;
		
		List<String> topicTypeList = new ArrayList<String> (Arrays.asList(topicTypes)) ;
		
		System.out.println("Debate" + "\t" + getHeader(topicTypeList));
		
		for ( String debate : debateListWithTopicType.keySet())
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append(debate);
			buffer.append("\t") ;
			
			Map<String,Double> typeMap = debateListWithTopicType.get(debate) ;
			double total = getTotalCount(typeMap) ;
			
			for ( String type : typeMap.keySet())
			{
				double value = (double) typeMap.get(type);
				value = (value /total ) *100d ;
				typeMap.put(type, value);
			}
			
			for (String type : topicTypeList)
			{
				Double value = typeMap.get(type);
				if (value == null )
				{
		//			System.out.println (" no topic type? check ") ;
					value = (double) 0 ;
				}
				
				buffer.append(value);
				buffer.append("\t") ;
			}
			
			System.out.println(buffer.toString()) ;
			
		}
		
		
		
		
	}
	
	
	
	private String getHeader(List<String> topicTypeList) 
	{
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer() ;
		for ( String type : topicTypeList)
		{
			buffer.append(type);
			buffer.append("\t") ;
		}
		return buffer.toString();
	}

	private double getTotalCount(Map<String, Double> typeMap) 
	{
		// TODO Auto-generated method stub
		double total =  0 ;
		for ( String type : typeMap.keySet())
		{
			total = total + typeMap.get(type);
		}
		return total;
	}

	private Map<String, Float> filterOnTopics(Map<String, Float> speechMap) 
	{
		// TODO Auto-generated method stub
		Map<String,Float> newMap = new HashMap<String,Float>() ;
		
		for ( String key : speechMap.keySet())
		{
			String type = topicCategoryMap.get(key);
			if ( type.equalsIgnoreCase("content"))
			{
				newMap.put(key, speechMap.get(key)) ;
			}
		}
		return newMap;
	}

	private Set<String> getContentTopics(Map<String, Integer> topicCategoryMaps) 
	{
		// TODO Auto-generated method stub
		Set<String> allTopics = new HashSet<String>() ;
		
		for ( String topic : topicCategoryMaps.keySet() )
		{
			if(topic.startsWith("content_"))
			{
				allTopics.add(topic);
			}
		}
		return allTopics;
	}

	private Map<String, Integer> getTopicCategoryMap(Set<String> noveltyFeatures) 
	{
		// TODO Auto-generated method stub
		Map<String,Integer> novelTopicTypeMap = new HashMap<String,Integer> () ;
		for ( String key : noveltyFeatures )
		{
			String category = topicCategoryMap.get(key) ;
			if ( null == category )
			{
				System.out.println("error in topic/category ");
				return null ;
			}
			Integer old = novelTopicTypeMap.get(category+ "_"+key);
			if ( null == old )
			{
				old = 0 ;
			}
			novelTopicTypeMap.put(category+"_"+key, old+1) ;
		}
		
		
		return novelTopicTypeMap;
	}

	private List<Integer> getPercentSplits ( int size )
	{
		List<Integer> percents = new ArrayList<Integer>();
		
		for ( int i = 0 ; i < 10 ; i++)
		{
			int posn = ( (size * 10) * (i+1) ) /100;
			percents.add(posn);
		}
		
		return percents ;
	}

	
	private Set<String> compareCumulativeAndCurrent(Set<String> cumulativeTopics,
			Map<String, Float> speechMap) 
	{
		// TODO Auto-generated method stub
		int count = 0 ;
		Set<String> novels = new HashSet<String>() ;
		
		for ( String topic : speechMap.keySet() )
		{
			float v = speechMap.get(topic) ;
			 if ( v > LDA_TOPIC_PROB_THRESH)
			 {
				 if ( !cumulativeTopics.contains(topic))
				 {
					 novels.add(topic);
				 }
			 }
		}
		return novels;
	}

	public List<String> loadUserNames ( String path, String file ) throws IOException
	{
		List<String> names = new ArrayList<String>() ;
		BufferedReader reader = new BufferedReader ( new FileReader ( path + file)) ;
		
		while ( true )
		{
			String line = reader.readLine() ;
			if ( null == line )
			{
				break ;
			}
			names.add(line) ;
		}
		reader.close() ;
		return names ;
	}
	
	public static void main(String[] args) throws Exception 
	{
		// TODO Auto-generated method stub
		String outputDir = args[0] ;
		String extn = args[1] ;
		
		TopicModelCrimeFeatureGenerator topicModelObj = new TopicModelCrimeFeatureGenerator();
		topicModelObj.setOutputDirAndExtn(outputDir, extn);
		
	//	String userNames = "black_usernames.txt" ;
		String userNames = "white_usernames.txt" ;
	//	String file = "alivewhileblack.tsv.04012015.filtered" ;
		String file = "crimingwhilewhite.tsv.04012015.filtered" ;
		file = "top-weighted-in-degree-cww.csv" ;
		file = "top-weighted-in-degree-awb.csv" ;
		

		
		String configPath = "./data/config/" ;
		
		//topics based on users
	//	List<String> users = topicModelObj.loadUserNames(configPath,userNames);
	//	topicModelObj.findTopicsBasedOnUsers(file) ;
		

		topicModelObj.findTopicsBasedOnTopRetweets(file) ;
		
		
	}

}
