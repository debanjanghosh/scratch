package com.research.course.debate.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.research.course.debate.rsrelations.Pair;

import gnu.trove.list.linked.TByteLinkedList ;

public class CreatingLanguageModel {

//	List<String> unigrams ;
//	List<String> bigrams ;
	
	int globalUnigPosn ;
	int globalBigPosn ;
	
	HashMap<String,Integer> unigrams ;
	HashMap<String,Integer> bigrams ;
	
	Map<Integer,Integer> bigramPairMap ;
	private ArrayList<String> stopWords;
	private HashMap<String, Map<Integer, Integer>> wekaBigramMap;
	private Integer lineNumber;
	
	public CreatingLanguageModel()
	{
		globalUnigPosn = -1 ;
		globalBigPosn = -1 ;
		
		unigrams = new HashMap<String,Integer>();
		bigrams = new HashMap<String,Integer>();
		wekaBigramMap = new HashMap<String,Map<Integer,Integer>>() ;
		lineNumber  = 1 ;
	}
	
	public String createBigramPairs ( String first, String second, String fold, String polRep )
	{
		String firstFeatures[] = first.split("\\s++");
		String secondFeatures[] = second.split("\\s++");
		
		int fPosn = 0 ;
		int sPosn = 0 ;
		
		bigramPairMap = new HashMap<Integer,Integer>();
		
		//smoothing is important here!
		//lets check the actual "ret" text
		String retText = " " ;
		List<Integer> posnList = new ArrayList<Integer>() ;
		
		for ( int i = 0 ; i < firstFeatures.length ; i ++ )
		{
			String f = firstFeatures[i] ;
			//need some cleaning 
			if(!TextUtilFunctions.checkAlphaNumeric(f))
			{
				continue ;
			}
			
			if(stopWords.contains(f))
			{
		//		continue ;
			}
			
			fPosn = getUnigramPosn(f,fold);
			if ( fPosn == -1 )
			{
				//i.e. the unigram is unsees
				//return (no smoothing?)
				continue ;
			}
			
			for ( int j = 0 ; j < secondFeatures.length ; j++ )
			{
				String s = secondFeatures[j];
				if(!TextUtilFunctions.checkAlphaNumeric(s))
				{
					continue ;
				}
				
				if(stopWords.contains(s))
				{
		//			continue ;
				}
				
				sPosn = getUnigramPosn(s,fold);
				if ( sPosn == -1 )
				{
					continue ;
				}
				
				String bigram = f + "_" + s ;
				int bPosn = getBigramPosn(bigram,fold);
				if ( bPosn == -1 )
				{
					continue ;
				}
				
				Integer old = bigramPairMap.get(bPosn);
				if ( old == null )
				{
					old = 0 ;
				}
				else
				{
		//			System.out.println("here") ;
				}
				bigramPairMap.put(bPosn,old+1);
				
				retText = retText  + "< " + bigram + " >" + " ";
				/*if (bigram.contains("the_this"))
				{
					System.out.println("here") ;
				}*/
			}
		}
		
//		System.out.println(retText);
		TreeMap<Integer,Integer> indexOrder = new TreeMap<Integer,Integer>(bigramPairMap);
		wekaBigramMap.put(polRep + "_" + lineNumber, indexOrder) ;
		lineNumber++ ;
		String ret = " " ;
		
		for ( Integer p : indexOrder.keySet() )
		{
			//System.out.println(p+"\t"+indexOrder.get(p)) ;
			//log value?
			int val = indexOrder.get(p);
			double logVal = Math.log((double)val);
			
			ret += p +":"+val+" ";
		//	ret += p +":"+logVal+" ";
		}
		
		retText = retText.trim();
		ret = ret.trim();
		return ret ;
	}
	
	public void createWekaBigramData(String path, String fold) throws IOException
	{
		BufferedWriter writer = new BufferedWriter
			    (new OutputStreamWriter(new FileOutputStream(path + "/" + "weka-output-0208"+ fold + ".arff"),"UTF-8"));
		
		writer.write("@relation debate-politics");
		writer.newLine();
		
		Map<String, Integer> sortedBigrams = sortByValues(bigrams);
		
		int bigramSize = sortedBigrams.size() ;
		
		for ( String bigram : sortedBigrams.keySet() )
		{
			//write the attributes
			writer.write("@attribute " + "\"" +  bigram + "\"" + " { t}") ;
			writer.newLine();
		}
		//write the relations
		writer.write("@attribute 'total' { D, R}"); 
		writer.newLine();
		writer.write("@data");
		writer.newLine();
		
		//write the data points 
		for ( String lineNum : wekaBigramMap.keySet())
		{
			String pol = lineNum.split("_")[0];
			Map<Integer,Integer> sortedBigramPosn = wekaBigramMap.get(lineNum);
			
			Set<Integer> sortedSetBigramPosn = sortedBigramPosn.keySet() ;
			String ret = " " ;
			for (  int posn = 0 ;posn < bigramSize ; posn++ )
			{
				if(sortedSetBigramPosn.contains(posn))
				{
					ret = ret + "t" + "," ;
				}
				else
				{
					ret = ret + "?" + "," ;
				}
			}
			ret = ret + pol + " " ;
			ret = ret.trim() ;
			writer.write(ret);
			writer.newLine();
		}
		
		writer.close();
	}

	public String getWekaFormatDataPoint()
	{
		return null ;
	}
	
	
	private int getUnigramPosn(String f, String fold) 
	{
		// TODO Auto-generated method stub
		if(!unigrams.containsKey(f))
		{
			if (fold.contains("train"))
			{
				globalUnigPosn++ ;
				unigrams.put(f,globalUnigPosn);
			}
			else
			{
				//unigrams does not contains the word
				//and we are not training
				//return
				return -1 ;
			}
			
		}
		
		int fPosn = unigrams.get(f);
		return fPosn;
	}
	
	private int getBigramPosn(String f, String fold) 
	{
		// TODO Auto-generated method stub
		if(!bigrams.containsKey(f))
		{
			if (fold.contains("train"))
			{
				globalBigPosn++ ;
				bigrams.put(f,globalBigPosn);
			}
			else
			{
				//bigrams does not contains the word
				//and we are not training
				//return
				return -1 ;
			}
			
		}
		
		int fPosn = bigrams.get(f);
		return fPosn;
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
	
	public static <K, V extends Comparable> Map sortByValues(final Map<String,Integer> map) {
		 
	    Comparator valueComparator = new Comparator<K>() {
		   public int compare(K k1, K k2) {
	 
		   int compare = ( map.get(k1)).compareTo( map.get(k2));
	 
		   if (compare == 0) {
			   return 1;
		   } else {
			   return compare;
		   }
	    }  
	   };
	 
	   Map sortedByValues = new TreeMap<K, V>(valueComparator);
	   sortedByValues.putAll(map);
	 
	   return sortedByValues;
	}
	
	
}
