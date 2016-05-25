package com.rutgers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrTokenizer;

public class TextUtility 
{
	private enum Markers
	{
		rt, HTTP, HTTPS, http,https
	}
	
	private static final String HASH = "#" ;
	
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
	
	public static int getHashPosition ( String[] words, String hash )
	{
		int index = 0 ;
		int hashIndex = -1 ;
		hash = "#" + hash ;
		
		for (String word : words )
		{
			word = word.toLowerCase() ;
			if(word.isEmpty())
			{
				continue ;
			}
			if(hash.equalsIgnoreCase(word.trim()))
			{
				hashIndex = index ;
			}
			
			index++ ;
		}
		index = 0 ;
		
		if(hashIndex == -1 )
		{
			//probably it is part of a word?
			
			for (String word : words )
			{
				word = word.toLowerCase() ;
				if(word.isEmpty())
				{
					continue ;
				}
				if ( word.contains(hash))
				{
					hashIndex = index ;
				}
				
				index++ ;
			}
			
		}
		
		return hashIndex ;
	}
	
	public static Boolean checkHashPosition ( String[] words, String hash )
	{
		int total = 0 ;
		int index = 0 ;
		int hashIndex = 10000 ; 
		for (String word : words )
		{
			if(word.isEmpty())
			{
				continue ;
			}
			if(hash.equalsIgnoreCase(word.trim()))
			{
				hashIndex = index ;
			}
			
			index++ ;
		}
		
		if( ( hashIndex <=2 ) && (index <=2) )
		{
			return true ;
		}
		
		return false ;
	}

	public static Boolean checkURL(String[] words) 
	{
		// TODO Auto-generated method stub
		int index = 0 ;
		int urlIndex = Integer.MAX_VALUE ;
				
		for ( String word : words )
		{
			if(word.equalsIgnoreCase(Markers.rt.toString()))
			{
				continue ;
			}
			
			if(word.contains(Markers.HTTP.toString() +"://") || word.contains(Markers.HTTPS.toString()+"://") ||
					word.contains(Markers.http.toString()+"://") || word.contains(Markers.https.toString()+"://"))
			{
				urlIndex = index ;
			}
			
			index++ ;
		}
		
		//a short tweet with URL in the beginning - a lot of times they are garbage
		if( (urlIndex <=2 ) && (index <=2  ) )
		{
			return true  ;
		}
		
		return false ;
	}
	public static String checkRT(String[] words) 
	{
		// TODO Auto-generated method stub
		StringBuffer ret = new StringBuffer() ;
		
		for ( String word : words )
		{
			if(word.equalsIgnoreCase(Markers.rt.toString()))
			{
				continue ;
			}
			if ( word.startsWith("@"))
			{
				word = "ToUser" ;
			}
			if(word.contains(Markers.HTTP.toString() +"://") || word.contains(Markers.HTTPS.toString()+"://") ||
					word.contains(Markers.http.toString() +"://") || word.contains(Markers.https.toString() +"://") 	)
			{
				word = "URL" ;
			}
			ret.append(word);
			ret.append(" ") ;
		}
		
		return ret.toString().trim() ;
	}

	 public static String allUppercase ( String token )
	 {
		 StringBuffer buffer = new StringBuffer() ;
		 char[] chars = token.toCharArray() ;
		 boolean flag = false ;	
		 for ( char c : chars )
		 {
			if (  'a' <= c && c <= 'z'  )
			{
				token = token.toLowerCase() ;
				return token ;
			}
			else if (  'A' <= c && c <= 'Z'  )
			{	
				flag = true ;
			}
			else 
			{
				flag = false ;
				token = token.toLowerCase() ;
				return token ;
			}
		}
	    	
	    if (flag)
	    {
	    		return token ;
	    }
	    
	    return token.toLowerCase();
	 }

	 public static boolean checkUppercase ( String token )
	    {
	    	//if all characters are upper case - return true
	    	char[] chars = token.toCharArray() ;
	    	
	    	for ( char c : chars )
			{
				if (  'a' <= c && c <= 'z'  )
				{
					return false ; 
				}
				
			}
	    	
	    	return true ;
	    }
	
	public static Boolean hashFilter(String[] words, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		for ( String word : words )
		{
			for ( String hash : hashes )
			{
				if(word.contains(hash))
				{
					return true ;
				}
			}
		}
		return false;
	}
	
	public static Boolean hashFilter(String[] words, String hash) 
	{
		// TODO Auto-generated method stub
		for ( String word : words )
		{
			if(word.contains(hash))
			{
				return true ;
			}
			
		}
		return false;
	}

	public static Boolean checkHashPosition(String[] words, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		int index = 0 ;
		int hashIndex = Integer.MAX_VALUE ; 
		for (String word : words )
		{
			if(word.isEmpty())
			{
				continue ;
			}
			for ( String hash : hashes )
			{
				if(hash.equalsIgnoreCase(word.trim()))
				{
					if(!(index > hashIndex))
					{
						hashIndex = index ;
					}
				}
			}
			
			index++ ;
		}
		
		if( ( hashIndex <=2 ) && (index <=2) )
		{
			return true ;
		}
		
		return false ;
	}
	
	
	public static String removePeriods ( String tweet)
	{
		String[] tokens = tweet.split("\\s++") ;
		StringBuffer ret = new StringBuffer() ;
		for ( String token : tokens )
		{
			
			boolean ch = checkAlphaNumeric(token) ;
			if (!ch)
			{
				continue ;
			}
			
			
			ret.append(token);
			ret.append(" ");
		}
		
		return ret.toString().trim();
	}
	
	
	public static String modifyNumbers ( String text)
	{
		StrTokenizer tokenizer = new StrTokenizer(text) ;
		String[] tokens = tokenizer.getTokenArray();
		StringBuffer ret = new StringBuffer() ;
		for ( String token : tokens )
		{
			boolean number = NumberUtils.isNumber(token);
			if(number)
			{
				token = "22" ; //all numbers are 22!
			}
			ret.append(token);
			ret.append(" ");
		}
		
		return ret.toString().trim();
	}
	
	public static String filteringForLucene(String[] tokens)
	{
		StringBuffer ret = new StringBuffer() ;
		for ( String token : tokens )
		{
			if (token.startsWith("@"))
			{
				token = "@ToUser" ;
			}
			boolean number = NumberUtils.isNumber(token);
			if(number)
			{
				token = "22" ; //all numbers are 22!
			}
			
			if (token.startsWith("http") || token.startsWith("HTTP") )
			{
				token = "URL" ;
			}
			
			if(token.startsWith("://"))
			{
				continue ;
			}
			
			token = stripNonAlpha(token);
			
			if (token.length() < 3)
			{
				continue ;
			}
			
			ret.append(token);
			ret.append(" ");
		}
		
		return ret.toString().trim();
	}

	public static String removeHashes( String tweets, String[] hashes) 
	{
		// TODO Auto-generated method stub
		for ( String hash : hashes )
		{
			//
			tweets = tweets.replaceAll("(?i)"+hash, "");

			
			
			
//			tweets = tweets.replace(hash, "");
		}
		
		return tweets.trim() ;
	}
	
	public static boolean checkTweetType ( String[] words, int position)
	{
		for ( int i = words.length-1 ; i>position ;i--)
		{
			String word = words[i];
			if(!word.contains(HASH))
			{
				if(word.contains(Markers.HTTP.toString() +"://") || word.contains(Markers.HTTPS.toString()+"://") ||
						word.contains(Markers.http.toString() +"://") || word.contains(Markers.https.toString() +"://") ||
						word.contains("@"))
				{
					continue ;
				}
				boolean alphaNumeric = checkAlphaNumeric(word);
				if(!alphaNumeric)
				{
					continue ;
				}
				
				
				return false ;
			}
		}
		
		return true ;
	}
	
	public static int lastHashPosition ( String[] words, List<String> hashes )
	{
		int lastPosition = -1 ;
		for ( String hash : hashes )
		{
			for ( int i = 0 ; i < words.length ; i++ )
			{
				String lower = words[i].toLowerCase();
				
				if(lower.contains(hash))
				{
					lastPosition = i ; 
				}
				
				
				String strip = stripNonAlpha(lower);
				//we do this because hashtags may contain random cases (upper/lower) or other
				//period marks etc.
				if(hash.equalsIgnoreCase(strip))
				{
					lastPosition = i ; 
				}
			}
		}
		
		return lastPosition ;
	}
	
	
	public static String removeHashes(String tweets, String hash) 
	{
		// TODO Auto-generated method stub
		tweets = tweets.replaceAll("(?i)"+hash, "");
		return tweets ;
	}

	public static String getMessageType(String hash) 
	{
		// TODO Auto-generated method stub
		hash = "#" + hash ;
		String [] positives = {"#excited",  "#grateful", "#happy" ,
				  "#joy" , "#loved", "#love", "#lucky", 
				  "#wonderful", "#positive", "#positivity"} ;
		
		List<String> posHashes = new ArrayList<String>(Arrays.asList(positives));
		
		
		String [] negatives = { "#angry" , "#awful" , "#disappointed" ,
				 "#fear" ,"#frustrated", "#hate",
				  "#sad", "#scared", "#stressed",
				  "#disappointed"} ;
		
		List<String> negHashes = new ArrayList<String>(Arrays.asList(negatives));
		
		
		String [] sarcasm = {"#sarcasm", "#sarcastic", "#irony" } ;
		
		
		List<String> sarcHashes = new ArrayList<String>(Arrays.asList(sarcasm));
		
		String [] random = {"#random" } ;
		List<String> randomHashes = new ArrayList<String>(Arrays.asList(random));
		
		
			
		if ( posHashes.contains(hash))
		{
			return "2" ;
		}
		else if ( negHashes.contains(hash))
		{
			return "3" ;
		}
		else if ( sarcHashes.contains(hash))
		{
			return "1" ;
		}
		else if ( randomHashes.contains(hash))
		{
			return "2" ;
		}
		
		return null;
	}

	public static boolean CheckNonAlpha(String token)
	{
		boolean ret = checkAlphaNumeric(token);
		if (ret)
		{
			return ret ;
		}
		boolean notdot = false ;
		if (!ret)
		{
			//this is a non alpha/numeric -
			//if it is a smiley we should keep it!
			char[] chars = token.toCharArray() ;
			for ( char c : chars )
			{
				//so it is not only periods!
				if (c != '.')
				{
					notdot = true ;
					break ;
				}
			}
			if (notdot && token.length() > 1)
			{
				return false ;
			}
			else
			{
				return true ;
			}
		}
		return true ;
		
	}
	
	public static boolean  checkURLUser(String[] tokens) 
	{
		// TODO Auto-generated method stub
		
		for ( String word : tokens )
		{
			if ( !(word.contains("ToUser")) || (word.contains("URL")) )
			{
				return true ;
			}
			
		}
		
		
		
		return false;
	}
	
	public static String stripNonAlpha ( String word )
	{
		StringBuffer ret = new StringBuffer();
		
		char[] chars = word.toCharArray() ;
		for ( char c : chars )
		{
			if(Character.isAlphabetic(c) || Character.isDigit(c) || c == '#' || c == '@' || c == '_' || c == '-'
					|| c == '!')
			{
				ret.append(c);
			}
			
		}
		return ret.toString().trim() ;
	}

	public static boolean checkAlphaNumeric(String rTRemoved) 
	{
		// TODO Auto-generated method stub
		char[] chars = rTRemoved.toCharArray() ;
		for ( char c : chars )
		{
			if(Character.isAlphabetic(c))
			{
				return true ;
			}
			if(Character.isDigit(c))
			{
				return true ;
			}
		}
		return false ;
	}

	public static boolean checkHashPresence(String tweet, List<String> hashes) 
	{
		// TODO Auto-generated method stub
		for ( String hash : hashes )
		{
			if(tweet.contains(hash))
			{
				return true ;
			}
		}
		return false;
	}

	public static boolean checkHashOrURL(String[] words, int hashPosition) 
	{
		// TODO Auto-generated method stub
		boolean hash = true ;
		int wrong = 0 ;
		for ( int i = hashPosition+1 ; i < words.length ; i++ )
		{
			String word = words[i] ;
			word = word.toLowerCase() ;
			if (word.startsWith("#") || word.startsWith("http"))
			{
				continue ;
			}
			else
			{
				if (wrong > 1)
				{
					hash = false ;
					break ;
				}
				wrong++ ;
			}
		}
		return hash;
	}

}
