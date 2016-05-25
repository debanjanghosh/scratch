package com.rutgers.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public  class CosineSimUtility 
{

	private static HashMap<Double, Double> unigramIdfMap;


	public static void setUnigramMap( HashMap<Double, Double > idfMap )
	{
		unigramIdfMap = idfMap ;
	}
	
	public static double getIdf(double context)
	{
		Double value = unigramIdfMap.get(context);
		if ( null == value )
		{
			System.out.println("unigram do not contain this context - check ");
			return 0.0 ;
		}
		return value ;
	}
	
	public static double getIdfVector(List<Double> vector)
	{
		double sum = 0.0;
		//|A| = a_i*a_i + ... + a_n*a_n
		for ( double v : vector)
		{
			sum = sum + getIdf(v) * getIdf(v) ;
		}
		
		return sum ;
	}

	public static double innerProductWithTfIdf(List<Double> tVector,List<Double> cVector)
	{
		double sum = 0.0;
		for ( double context : cVector)
		{
			if ( tVector.contains(context))
			{
				sum = sum + getIdf(context) * getIdf(context) ;
			}
		}
		
		//normalize it
		double tSizeNorm = getIdfVector(tVector) ;
		double cSizeNorm = getIdfVector(cVector) ;
		
		
		
		
		if ( tSizeNorm == 0.0 || cSizeNorm == 0.0)
		{
			return 0.0 ;
		}
		
	//	return sum ;
		
		
		return sum/(Math.sqrt(tSizeNorm) * Math.sqrt(cSizeNorm));
	}
	
	public static double innerProductWithRankPosition(String target, 
			Map<Double, Double> targetRankVector,List<Double> cVector)
	{
		double sum = 0.0;
		for ( double context : cVector)
		{
			if ( targetRankVector.keySet().contains(context))
			{
				Double rank = targetRankVector.get(context) ;
				if( null == rank)
				{
					System.out.println("context not available") ;
					rank = 0.0 ;
				}
				
				sum = sum + rank.doubleValue()  * rank.doubleValue()  ;
			}
		}
		
		//normalize it
		double tSizeNorm = 0;//getRankVector(tVector,positionRankMap) ;
		double cSizeNorm = 0;//getRankVector(cVector,positionRankMap) ;
		
		
		
		
		if ( tSizeNorm == 0.0 || cSizeNorm == 0.0)
		{
			return 0.0 ;
		}
		
	//	return sum ;
		
		
		return sum/(Math.sqrt(tSizeNorm) * Math.sqrt(cSizeNorm));
	}
	
	public double getRankVector(List<Double> vector, Map<Double,Double> positionRankMap)
	{
		double sum = 0.0;
		//|A| = a_i*a_i + ... + a_n*a_n
		for ( double v : vector)
		{
			Double value = positionRankMap.get(v);
			if ( null == value )
			{
				value = 0.0 ;
			}
			sum = sum + value.doubleValue() * value.doubleValue() ;
		}
		
		return sum ;
	}
	
	
	public static double innerProduct(List<Double> tVector,List<Double> cVector)
	{
		double sum = 0.0;
		for ( double context : cVector)
		{
			if ( tVector.contains(context))
			{
				sum = sum + 1.0 ;
			}
		}
		
		//normalize it
		double tSize =tVector.size() ;
		double cSize =cVector.size() ;
		
		if ( tSize == 0.0 || cSize == 0.0)
		{
			return 0.0 ;
		}
		
	//	return sum ;
		
		
		return sum/(Math.sqrt(tSize) * Math.sqrt(cSize));
	}
	
}
