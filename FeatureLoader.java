package com.deft.sarcasm.features;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


abstract public class FeatureLoader 
{
	public abstract Map<Integer, Double> loadFeatures(String[] tokens) ;
	public abstract Map<String, Double> loadNonLexFeatures(String[] tokens) ;

//	public abstract void close() throws IOException ;
	
	public List<Integer> getFeatureVector(Map<Integer, Integer> featureMap) 
	{
		// TODO Auto-generated method stub

		// sort the columns using TreeMap
		TreeMap<Integer, Integer> featureMapSorted = new TreeMap<Integer, Integer>(
				featureMap);

		String f = null ;
		
		List<Integer> features = new ArrayList<Integer>() ;
		
		for (Integer feature : featureMapSorted.keySet()) 
		{
			Integer val = featureMapSorted.get(feature);
			if (null == val) 
			{
				// something wrong - throw exception later
				System.out.println("something wrong in feature value, check");
				continue;
			}

		//	f = feature + ":" + "1" ; //1 = binary
			features.add((feature));
		

		}

		return features;
	}
	public List<Integer> getPuncFVs(int xclm, int ques, int quotes) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	public Map<Integer, Double> loadFeatures(String[] tokens, String target) {
		// TODO Auto-generated method stub
		return null;
	}
	

	
}
