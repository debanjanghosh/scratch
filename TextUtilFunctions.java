package com.research.course.debate.util;

import java.util.HashSet;
import java.util.Set;

public class TextUtilFunctions {

	public static boolean checkAlphaNumeric(String token) 
	{
		// TODO Auto-generated method stub
		//anything below three letters - no good
		if (token.length() < 3)
		{
			return false ;
		}
		
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
	
	public static Set<String> common( Set<String> first, Set<String> second)
	{
		Set<String> intersection = new HashSet<String>(first);
		intersection.retainAll(second);
		return intersection ;
	}
	
	public static Set<String> uncommon( Set<String> first, Set<String> second)
	{
		Set<String> notcommon = new HashSet<String>();
	
		Set<String> firstCopy = new HashSet<String>(first);
		Set<String> secondCopy = new HashSet<String>(second);
		
		firstCopy.removeAll(secondCopy);
		notcommon.addAll(firstCopy);
		
		firstCopy = new HashSet<String>(first);
		secondCopy.removeAll(firstCopy);
		notcommon.addAll(secondCopy);
		
		return notcommon ;
	}
	
	
	
	
}
