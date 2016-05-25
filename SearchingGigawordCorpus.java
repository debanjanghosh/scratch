package com.rutgers.justi.lucene.vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import com.rutgers.util.Counter;
import com.rutgers.util.CounterMap;

public class SearchingGigawordCorpus {

	CounterMap<String, String> contextCounter = new CounterMap<String, String>();

	private ArrayList<String> uniqueVerbsFromPDTB;
	
	public static  String FILES_TO_INDEX_DIRECTORY  ;
	

	/**
	 * @param args
	 */
	
	private static final int AVAILABLE_PROCESSORS = 10; //Runtime.getRuntime().availableProcessors();
	  
	public static final String INDEX_DIRECTORY = "./data/gigaindex/.";
	public static final String TEXT_FIELD = "text_field";


	private FSDirectory directory;
	
	private IndexSearcher[] searchers ;

	private IndexSearcher isearcher;
	
	
	public SearchingGigawordCorpus() throws IOException
	{
		uniqueVerbsFromPDTB = new ArrayList<String>();

		directory = FSDirectory.open(new File(INDEX_DIRECTORY));

	}
	
	private void searchIndexesBySpan() throws IOException, ParseException 
	{
		java.util.Date date2 = new java.util.Date();
		System.out.println("SEARCH STARTED ");
		System.out.println("TIME IS: " + new Timestamp(date2.getTime()));

		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		for (String verb : uniqueVerbsFromPDTB) 
		{
			verb = verb.toLowerCase();
	//		verb = stemIt(verb) ;
			// stemmer.setCurrent(verb);
			// stemmer.stem();
			// verb = stemmer.getCurrent() ;
			SpanTermQuery fleeceQ = new SpanTermQuery(
					new Term(TEXT_FIELD, verb));
		//	TopDocs results = isearcher.search(fleeceQ, 100000);

			IndexReader reader = isearcher.getIndexReader();
			AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
			Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
			Spans spans = fleeceQ.getSpans(wrapper.getContext(),
					new Bits.MatchAllBits(reader.numDocs()), termContexts);

			int window = 5;// get the words within two of the match
			while (spans.next() == true) 
			{
				// build up the window
				Map<String, Integer> entries = new TreeMap<String, Integer>();
				// System.out.println("Doc: " + spans.doc() + " Start: " +
				// spans.start() + " End: " + spans.end());

				int start = spans.start() - window;
				int end = spans.end() + window-1;
				Terms content = reader.getTermVector(spans.doc(), TEXT_FIELD);
				TermsEnum termsEnum = content.iterator(null);
				BytesRef term;

				while ((term = termsEnum.next()) != null) 
				{
					// could store the BytesRef here, but String is easier for
					// this example
					String s = new String(term.bytes, term.offset, term.length);
					DocsAndPositionsEnum positionsEnum = termsEnum
							.docsAndPositions(null, null);
					if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) 
					{
						int i = 0;
						int position = -1;
						while (i < positionsEnum.freq()
								&& (position = positionsEnum.nextPosition()) != -1) 
						{
							if (position >= start && position <= end) 
							{
								entries.put(s, position);
							}
							i++;
						}
					}
				}
				loadEntries(verb, entries);
		//		System.out.println("Entries:" + entries);
			
			}
			
			
			
			System.out.println("VERB PROCESSING FINISHED " + verb);
			writeVerb(verb);
			java.util.Date date3 = new java.util.Date();
			System.out.println("TIME IS: " + new Timestamp(date3.getTime()));
			
		
		}

		ireader.close();
		directory.close();
		
	}
	
	public void searchForVerbs ( String verb) throws IOException
	{
		System.out.println("VERB PROCESSING STARTED " + verb);
		java.util.Date date3 = new java.util.Date();
		System.out.println("TIME IS: " + new Timestamp(date3.getTime()));

		
		SpanTermQuery fleeceQ = new SpanTermQuery(
				new Term(TEXT_FIELD, verb));
	//	TopDocs results = isearcher.search(fleeceQ, 100000);

		IndexReader reader = isearcher.getIndexReader();
		AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
		Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
		Spans spans = fleeceQ.getSpans(wrapper.getContext(),
				new Bits.MatchAllBits(reader.numDocs()), termContexts);

		int window = 5;// get the words within two of the match
		while (spans.next() == true) 
		{
			// build up the window
			Map<String, Integer> entries = new TreeMap<String, Integer>();
			// System.out.println("Doc: " + spans.doc() + " Start: " +
			// spans.start() + " End: " + spans.end());

			int start = spans.start() - window;
			int end = spans.end() + window-1;
			Terms content = reader.getTermVector(spans.doc(), TEXT_FIELD);
			TermsEnum termsEnum = content.iterator(null);
			BytesRef term;

			while ((term = termsEnum.next()) != null) 
			{
				// could store the BytesRef here, but String is easier for
				// this example
				String s = new String(term.bytes, term.offset, term.length);
				DocsAndPositionsEnum positionsEnum = termsEnum
						.docsAndPositions(null, null);
				if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) 
				{
					int i = 0;
					int position = -1;
					while (i < positionsEnum.freq()
							&& (position = positionsEnum.nextPosition()) != -1) 
					{
						if (position >= start && position <= end) 
						{
							entries.put(s, position);
						}
						i++;
					}
				}
			}
			loadEntries(verb, entries);
	//		System.out.println("Entries:" + entries);
		
		}
		
		
		
	//	System.out.println("VERB PROCESSING FINISHED " + verb);
		writeVerb(verb);
		
	//	java.util.Date date3 = new java.util.Date();
	//	System.out.println("TIME IS: " + new Timestamp(date3.getTime()));
		
	}
	
	private void searchIndexesBySpanMT() throws IOException, ParseException, InterruptedException, ExecutionException 
	{
		int numTasksSubmitted = 0;
		int numTasksRead = 0;

		java.util.Date date2 = new java.util.Date();
		System.out.println("SEARCH STARTED ");
		System.out.println("TIME IS: " + new Timestamp(date2.getTime()));

		DirectoryReader ireader = DirectoryReader.open(directory);
	
	//	IndexSearcher isearcher = new IndexSearcher(ireader);
	//	private static final ExecutorService executor = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
	
		final ExecutorService es = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
		 
		final ExecutorCompletionService<Object> executorService = new ExecutorCompletionService<Object>(es);
		
	
	   
		 isearcher = new IndexSearcher(ireader, es) ;
		 
		// es.invokeAll(tasks)
		
	//	ParallelMultiSearcher searcherObj = new  ParallelMultiSearcher(searchers);

		for (String verb : uniqueVerbsFromPDTB) 
		{
			final String verb1 = verb.toLowerCase();
	//		verb = stemIt(verb) ;
			// stemmer.setCurrent(verb);
			// stemmer.stem();
			// verb = stemmer.getCurrent() ;
			
			es.submit(new Runnable()
			{
				public void run()
				{

					try {
						searchForVerbs(verb1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, Boolean.TRUE);
			
			++numTasksSubmitted;
			
			
			
			numTasksRead = clearExecutorQueue( numTasksRead, executorService );
			
			
			if(shouldThrottle( numTasksSubmitted, numTasksRead ))
			{
				while(shouldThrottle( numTasksSubmitted, numTasksRead ))
				{
					
					executorService.take().get();
					++numTasksRead;
				}
			}
			else
			{
				//?
			}
		
		}

		ireader.close();
		directory.close();
		
		while(numTasksRead != numTasksSubmitted)
		{
			executorService.take().get();
		    ++numTasksRead;
		}
		
		 es.shutdown();
		 es.awaitTermination(1, TimeUnit.SECONDS);

		
	}
	
	 private static int clearExecutorQueue( int numTasksRead, final ExecutorCompletionService<Object> executorService )
		        throws InterruptedException, ExecutionException
		    {
		        Future<Object> f;
		        while((f = executorService.poll()) != null)
		        {
		        	f.get();
		        	++numTasksRead;
		        }
		        return numTasksRead;
		    }
	
	  private static boolean shouldThrottle( int numTasksSubmitted, int numTasksRead )
	    {
	        return numTasksSubmitted - numTasksRead > AVAILABLE_PROCESSORS;
	    }

	
	private void loadEntries(String verb, Map<String, Integer> entries) {
		// TODO Auto-generated method stub
		for (String entry : entries.keySet()) 
		{
			contextCounter.incrementCount(verb, entry, 1.0);
		}
	}
	
	public void loadPDTBVectors(String path, String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path + "/"
				+ file));

		while (true) {
			String line = reader.readLine();
			if (null == line) {
				break;
			}
			String features[] = line.split("\t");

			if (features.length != 3) {
				continue;
			}

			
			
			String relation = features[0];
			String verb1 = features[1].toLowerCase().trim();
			String verb2 = features[2].toLowerCase().trim();

			// if (verb1.equalsIgnoreCase(".the") || verv)

			// get unique verbs
			if (!uniqueVerbsFromPDTB.contains(verb1)) {
				if (verb1.contains("'") || verb1.contains("`")
						|| verb1.isEmpty() || verb1.contains(".")) {
					continue;
				}
				uniqueVerbsFromPDTB.add(verb1);

			}
			// get unique verbs
			if (!uniqueVerbsFromPDTB.contains(verb2)) {
				if (verb2.contains("'") || verb2.contains("`")
						|| verb2.isEmpty() || verb2.contains(".")) {
					continue;
				}
				uniqueVerbsFromPDTB.add(verb2);

			}

		}

		reader.close();

		java.util.Collections.sort(uniqueVerbsFromPDTB);

		System.out
				.println("SIZE OF VERB LIST IS " + uniqueVerbsFromPDTB.size());

		// for ( String v : uniqueVerbsFromPDTB)
		// {
		// System.out.println(v) ;
		// }
	}
	
	
	private void writeVerb(String verb) throws IOException 
	{
		// TODO Auto-generated method stub
		String output = "./data/output/gigavector/" ;
		String outputFile = "pdtb_verb_vector_nostem_11142014_" ;
	
		// File file = File.createTempFile(outputFile + verb, ".txt", new File (output))   ;
		 FileWriter writer = new FileWriter(output + outputFile + verb +".txt");
	     writer.write("verb" + "\t" + "context" + "\t" + "count" + "\t" + "total count" + "\n")    ;
		 
		Counter<String> windowCounter = contextCounter.getCounter(verb) ;
		
		double totalCount = windowCounter.totalCount() ;
			
		for ( String window : windowCounter.keySet())
		{
			double count = windowCounter.getCount(window) ;
			if ( count > 5.0 )
			{
				writer.write(verb + "\t" + window + "\t" + count + "\t" + totalCount + "\n") ;
			}
		}
		
		writer.flush();
		writer.close();
		System.out.println("VERB WRITING FINISHED " + verb);
		java.util.Date date3 = new java.util.Date();
		System.out.println("TIME IS: " + new Timestamp(date3.getTime()));

		
		
	}
	
	public void activate ( String configFile) throws IOException
	{
		
		Properties prop = new Properties();
		InputStream input = null;
	 		
		input = new FileInputStream("./data/config/" + configFile);
	 
		// load a properties file
		prop.load(input);
	 
		// get the property value and print it out
	
		FILES_TO_INDEX_DIRECTORY = prop.getProperty("inputPath") ;
		
	
		input.close() ;
	}
	
	public static void main(String[] args) throws IOException, ParseException, InterruptedException, ExecutionException 
	{
		// TODO Auto-generated method stub
		String configParam = args[0].trim() ;
		if (!configParam.equalsIgnoreCase("-c") )
		{
			System.out.println("not correct parameter for config. Exit") ;
			return ;
		}
		
		String configFile = args[1] ;
		
		
		String pdtbPath = "./data/output/pdtb/";
		String pdtbFile = "pdtb2_ascii_all_0801.txt.verbs_deproot.txt.verbpairs_nostem.txt";

		SearchingGigawordCorpus indexSearchVectorObj = new SearchingGigawordCorpus() ;
		indexSearchVectorObj.activate(configFile);
		indexSearchVectorObj.loadPDTBVectors(pdtbPath, pdtbFile);
		indexSearchVectorObj.searchIndexesBySpanMT();

	}

}
