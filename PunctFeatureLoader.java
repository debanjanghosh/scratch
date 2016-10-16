package com.deft.sarcasm.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.deft.sarcasm.util.TextUtility;

public class PunctFeatureLoader extends FeatureLoader
{


	@Override
	public Map<Integer, Double> loadFeatures(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Integer> getPuncFVs(int xclm, int ques, int quotes) 
	{
		// TODO Auto-generated method stub
		List<Integer> puncs = new ArrayList<Integer>() ;
		if(xclm > 1)
		{
			puncs.add(90) ;
		}
		if(ques > 1)
		{
			puncs.add(91) ;
		}
		if(quotes > 1)
		{
			puncs.add(92) ;
		}
		return puncs;
	}
	
	public TreeMap<String,Double> getPuncFVs(String line) 
	{
		// TODO Auto-generated method stub
		
		double xclm = 0.0 ;
		double ques = 0.0 ;
		double quotes = 0.0 ;
		
		xclm = TextUtility.countChars(line,'!');
		ques = TextUtility.countChars(line,'?');
		quotes = TextUtility.countChars(line,'"');
	
		TreeMap<String,Double> puncs = new TreeMap<String,Double>() ;
		
		if(xclm > 0)
		{
			puncs.put("!_"+xclm,1.0) ;
		}
		if(ques > 0)
		{
			puncs.put("?_"+ques,1.0) ;
		}
		if(quotes > 1)
		{
			puncs.put("\"_"+quotes,1.0) ;
		}
		return puncs;
	}

	@Override
	public Map<String, Double> loadNonLexFeatures(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
