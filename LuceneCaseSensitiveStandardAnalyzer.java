package com.rutgers.util;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
//import org.apache.lucene


public class LuceneCaseSensitiveStandardAnalyzer extends Analyzer {
	private final Version version;
	private CharArraySet stopSet;

	public LuceneCaseSensitiveStandardAnalyzer(final Version version) 
	{
		this.version = version;
	}
	
	public LuceneCaseSensitiveStandardAnalyzer(final Version version,CharArraySet stopSet) 
	{
		this.version = version;
		this.stopSet = stopSet ;
	}
	
	@Override
	protected TokenStreamComponents createComponents(final String field,
			final Reader reader) 
	{
		
		//Tokenizer tokenizer = new StandardTokenizer(reader);
		Tokenizer tokenizer = new WhitespaceTokenizer(reader);
		TokenStream filter = new StopFilter(version,tokenizer, stopSet);
		return new TokenStreamComponents(tokenizer, filter);
	}
}
