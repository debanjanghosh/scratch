package com.rutgers.justi.lucene.vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class GigawordAnalyzer extends Analyzer
{
	 private CharArraySet stopWords = new CharArraySet(Version.LATEST,StopAnalyzer.ENGLISH_STOP_WORDS_SET,true);
	 
	 public GigawordAnalyzer() throws IOException
	 {
		 //add new words to stopWords
		 BufferedReader reader = new BufferedReader ( new FileReader ( "./data/config/stopwords.txt") );
		 while ( true )
		 {
			 String line = reader.readLine() ;
			 if ( null == line )
			 {
				 break;
			 }
			 stopWords.add(line.trim()) ;		 
		 }
		 
		 reader.close();
	 }
	 
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) 
	{
		// TODO Auto-generated method stub
		Tokenizer source = new LowerCaseTokenizer(Version.LUCENE_CURRENT  ,reader);
		TokenStream filter = new LowerCaseFilter(source);
				
			//	StopFilter(source, stopWords);
	    
	//    filter = new PorterStemFilter(filter);
	    
		return new TokenStreamComponents(source, filter);
		
	}
}
