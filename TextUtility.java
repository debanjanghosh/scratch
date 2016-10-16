package com.deft.sarcasm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TextUtility 
{
	private static ArrayList<String> hashTagList;

	public static int countChars(String line, char c) 
	{
		// TODO Auto-generated method stub
		int count =  0 ;
		char[] chars = line.toCharArray() ;
		
		for ( char x : chars )
		{
			if(x == c)
			{
				count++ ;
			}
		}
		return count;
	}

	public static String recreate(String[] features, int start, int length) 
	{
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer() ;
		for ( int i = start ; i < length ; i++ )
		{
			buffer.append(features[i]);
			buffer.append(" ") ;
		}
		
		return buffer.toString().trim();
	}
	
	public static boolean isAlphaNumeric ( String token )
	{
		char[] chars = token.toCharArray() ;
		for ( char c : chars )
		{
			if ( ( 'a' <= c && c <= 'z') || ( 'A' <= c && c <= 'Z') || ( '0' <= c && c <= '9')  )
			{
				return true ;
			}
		}
		return false ;
	}
	
	  /**
     * Strips all punctuation (non-alphanumeric characters) from a word and returns the word.
     * @param word The word to be modified
     * @return The word, stripped of all punctuation
     */
    public static String stripPunctuation(String word)
    {
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        return word;
    }

    public static boolean checkUppercase ( String token )
    {
    	//if all characters are upper case - return true
    	char[] chars = token.toCharArray() ;
    	
    	for ( char c : chars )
		{
			if ( ( 'A' <= c && c <= 'Z') )
			{
				//do nothing 
			}
			else
			{
				return false ;
			}
		}
    	
    	return true ;
    }
    
    
    public static boolean CheckNumeric ( String token )
	{
		StringBuffer buffer = new StringBuffer() ;
		char[] chars = token.toCharArray() ;
		
		for ( char c : chars )
		{
			if (( '0' <= c && c <= '9') )
			{
				
			}
			else
			{
				return false ;
			}
		}
		
		return true ;
	}
	
	public static String stripNonAlphaNumericAndSpecial ( String token )
	{
		StringBuffer buffer = new StringBuffer() ;
		char[] chars = token.toCharArray() ;
		
		for ( char c : chars )
		{
			if ( ( 'a' <= c && c <= 'z') || ( 'A' <= c && c <= 'Z') || ( '0' <= c && c <= '9') || (c == '@') ||
					(c == '#') )
			{
				buffer.append(c);		
			}
		}
		
		return buffer.toString() ;
	}
	
	public static void loadCrimeHashtags()
	{
		String [] crimeHashtags = {"#crimingwhilewhite" , "#alivewhileblack" } ;
		hashTagList = new ArrayList<String>(Arrays.asList(crimeHashtags)) ;

	}
	
	public static void loadHashtags()
	{
		String [] englishHashtags = {"#sarcasm", "#sarcastic" , "#angry" , "#awful" , "#disappointed" ,
				  "#excited", "#fear" ,"#frustrated", "#grateful", "#happy" ,"#hate",
				  "#joy" , "#loved", "#love", "#lucky", "#sad", "#scared", "#stressed",
				  "#wonderful", "#positive", "#positivity", "#disappointed", "#irony"} ;

//spanish
		String [] spanishHashtags = {"#sarcasmo", "#sarcasm" , "#feliz" , "#alegre" , "#entusiasmado" ,
	  "#contento", "#gratitud" ,"#diversion", "#amor", "#enamorado" ,"#optimismo",
	  "#triste" , "#enojado", "#irritado", "#aterrado", "#asustado", "#asustado", "#confundido"} ;


		hashTagList = new ArrayList<String>(Arrays.asList(spanishHashtags)) ;
		hashTagList.addAll(Arrays.asList(englishHashtags));
	}
	
	
	public static boolean checkAllHashtags ( String line )
	{
		if (line.contains(hashTagList.get(0) ) && line.contains(hashTagList.get(0) ) )
		{
			return true ;
		}
		else
		{
			return false ;
		}
		
	}

	
	public static String removeHashtags ( String line )
	{
		for ( String hash : hashTagList )
		{
			line = line.replaceAll("(?i)" +hash, "");
		}
		
		return line ;
	}

	public static boolean checkSarcasm(String[] tokens) 
	{
		// TODO Auto-generated method stub
		for ( String token : tokens )
		{
			if ( token.equalsIgnoreCase("sarcasm") || token.equalsIgnoreCase("sarcastic") )
			{
				return true ;
			}
		}
		
		
		return false;
	}
	
	public static Map<String,Double> extractSentiFeaturesAsMap(Map<String, Double> sentiMap)
	{
		if(sentiMap.get("totalArg1Positive") > 0 && ( sentiMap.get("totalArg1Negative") > 0 ||
				sentiMap.get("totalArg1NegatePositive") > 0) )
		{
			sentiMap.put("arg1Contrast", 1.0);
		}
		else
		{
			sentiMap.put("arg1Contrast", 0.0);
		}

		return sentiMap ;
	}

	public static String getValuesOfMap(Map<String, Double> sentiMap) 
	{
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer() ;
		int index = 1 ;

		for ( String key : sentiMap.keySet())
		{
			buffer.append(index + ":" +sentiMap.get(key));
			buffer.append(" ");
			index++ ;
		}
		
		if(sentiMap.get("totalArg1Positive") > 0 && ( sentiMap.get("totalArg1Negative") > 0 ||
				sentiMap.get("totalArg1NegatePositive") > 0) )
		{
			buffer.append(index+":"+"1.0");
			buffer.append("\t");
			index++ ;
		}
		else
		{
			buffer.append(index+":"+"0.0");
			buffer.append("\t");
			index++ ;

		}
		
		return buffer.toString().trim();
	}

	public static String getValuesOfMap(Map<String, Double> sentiMap1, Map<String, Double> sentiMap2) 
	{
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer() ;
		int index = 1 ;
		for ( String key : sentiMap1.keySet())
		{
			buffer.append(index+":"+sentiMap1.get(key));
			buffer.append(" ");
			index++ ;
		}
		
		for ( String key : sentiMap2.keySet())
		{
			buffer.append(index+":"+sentiMap2.get(key));
			buffer.append(" ");
			index++ ;
		}

		
		if(sentiMap1.get("totalArg1Positive") > 0 && ( sentiMap2.get("totalArg1Negative") > 0 ||
				sentiMap2.get("totalArg1NegatePositive") > 0) )
		{
			buffer.append(index+":"+"1:0");
			buffer.append(" ");
			index++ ;
		}
		else
		{
			buffer.append(index+":"+"0:0");
			buffer.append(" ");
			index++ ;

		}
	
		
		if(sentiMap1.get("totalArg1NegatePositive") > 0 && ( sentiMap1.get("totalArg1Negative") > 0 ||
				sentiMap2.get("totalArg1Positive") > 0) )
		{
			buffer.append(index+":"+"1:0");
			buffer.append("\t");
			index++ ;
		}
		else
		{
			buffer.append(index+":"+"0:0");
			buffer.append("\t");
			index++ ;

		}
		
		
		
		return buffer.toString().trim();
	}

	public static Map<String, Double> extractSentiFeaturesAsMap(Map<String, Double> sentiMap,int context) 
	{
		// TODO Auto-generated method stub
		for ( String key : sentiMap.keySet())
		{
			sentiMap.put(key, sentiMap.get(key));
		}
		
		if ( context == 0 )
		{
			if(sentiMap.get("totalArg1Positive") > 0 && ( sentiMap.get("totalArg1Negative") > 0 ||
					sentiMap.get("totalArg1NegatePositive") > 0) )
			{
				sentiMap.put("totalArg1Arg1Contrast", 1.0);
			}
			else
			{
				sentiMap.put("totalArg1Arg1Contrast", 0.0);

			}
		}
		
		else if ( context == 1 )
		{
		
			if(sentiMap.get("totalArg1Positive") > 0 && ( sentiMap.get("totalArg2Negative") > 0 ||
					sentiMap.get("totalArg2NegatePositive") > 0) )
			{
				sentiMap.put("totalArg1Arg2Contrast", 1.0);
			}
			else
			{
				sentiMap.put("totalArg1Arg2Contrast", 0.0);
	
			}
		
			
			if(sentiMap.get("totalArg1NegatePositive") > 0 && ( sentiMap.get("totalArg1Negative") > 0 ||
					sentiMap.get("totalArg2Positive") > 0) )
			{
				sentiMap.put("totalArg2Arg1Contrast", 1.0);
	
			}
			else
			{
				sentiMap.put("totalArg2Arg1Contrast", 0.0);
	
	
			}
		}
		
		
		return sentiMap ;
		
		
	}

	
}
