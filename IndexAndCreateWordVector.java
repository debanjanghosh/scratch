package com.rutgers.justi.lucene.vector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.print.attribute.standard.PDLOverrideSupported;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

//import org.apache.lucene.analysis.PorterStemmer;

import org.tartarus.snowball.ext.PorterStemmer;

import com.rutgers.util.Counter;
import com.rutgers.util.CounterMap;

public class IndexAndCreateWordVector {

	/**
	 * @param args
	 */

	public static  String FILES_TO_INDEX_DIRECTORY  ;
	public static final String INDEX_DIRECTORY = "./data/index/.";

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	public static final String TEXT_FIELD = "text_field";
	private FSDirectory directory;

	private Analyzer analyzer;

	private SentenceModel sentenceModel;
	private SentenceDetectorME sentenceDetector;

	private FileInputStream is;

	private PorterStemmer stemmer;
	private List<String> uniqueVerbsFromPDTB;

	CounterMap<String, String> contextCounter = new CounterMap<String, String>();

	public IndexAndCreateWordVector() throws IOException {
		stemmer = new PorterStemmer();
		is = new FileInputStream("./data/config/en-sent.bin");

		uniqueVerbsFromPDTB = new ArrayList<String>();

		directory = FSDirectory.open(new File(INDEX_DIRECTORY));

	}

	private List<String> checkSentenceBoundaries(List<String> lines) {
		// TODO Auto-generated method stub
		List<String> newLines = new ArrayList<String>();

		for (String line : lines) {
			String[] newlines = sentenceDetector.sentDetect(line);

			newLines.addAll(Arrays.asList(sentenceDetector.sentDetect(line)));
		}

		return newLines;
	}
	public List<String> getAllFiles ( String path )
	{
		List<String> fileNames = new ArrayList<String>() ;
		File dir = new File(FILES_TO_INDEX_DIRECTORY);
		System.out.println("FILE IN " + FILES_TO_INDEX_DIRECTORY) ;
		
		File[] files = dir.listFiles();

		for ( File file :  files )
		{
			if (file.isDirectory())
			{
				//now go for sub-files
				File[] subfiles = file.listFiles();
				for ( File subfile : subfiles )
				{
					String full = subfile.getAbsolutePath() ;
					fileNames.add(FILES_TO_INDEX_DIRECTORY + "/" + file.getName() + "/" + subfile.getName()) ;
				}
			}
		}
		
		return fileNames ; 
	}

	public void createIndex() throws CorruptIndexException,
			LockObtainFailedException, IOException {
		java.util.Date date1 = new java.util.Date();
		System.out.println("INDEXING STARTED ");
		System.out.println("TIME IS: " + new Timestamp(date1.getTime()));

		sentenceModel = new SentenceModel(is);
		sentenceDetector = new SentenceDetectorME(sentenceModel);

		CharArraySet stopSet = StandardAnalyzer.STOP_WORDS_SET;
		
	//	stopSet.add(text)
		
		analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT,stopSet);
	//	analyzer = new SnowballAnalyzer(Version.LUCENE_CURRENT) ;
		
	//	analyzer = new GigawordAnalyzer() ;

		// directory = new RAMDirectory();

		boolean recreateIndexIfExists = true;
		IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_CURRENT, analyzer);

		IndexWriter indexWriter = new IndexWriter(directory, config);
		
		List<String> allFiles = getAllFiles(FILES_TO_INDEX_DIRECTORY) ;

		
		BufferedReader reader = null;
		
		for (String file : allFiles) 
		{
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF8"));

			System.out.println("INDEXING FILE IS " + file) ;
			
			
			List<String> lines1 = Files.readAllLines(
					Paths.get(file),
					StandardCharsets.UTF_8);

			// each line contains may small sentences -
			// we need to find sentence boundaries here!

			List<String> newLines = checkSentenceBoundaries(lines1);
			Document document = null;
			int lineNum = 1;
			for (String newLine : newLines) 
			{
				document = new Document();
				String text = newLine.toLowerCase();
				// String tokens[] = text.split("\\s++") ;
				// String stemmedText = stemIt(tokens);

				Field id = new Field("id", "doc_" + file + "_"
						+ lineNum, Field.Store.YES,
						Field.Index.NOT_ANALYZED_NO_NORMS);
				document.add(id);
				// Store both position and offset information
				Field gigaLine = new Field(TEXT_FIELD, text, Field.Store.NO,
						Field.Index.ANALYZED,
						Field.TermVector.WITH_POSITIONS_OFFSETS);
				document.add(gigaLine);
				indexWriter.addDocument(document);

			}

		}
		// indexWriter.optimize();
		indexWriter.close();

		java.util.Date date2 = new java.util.Date();
		System.out.println("INDEXING FINISHED ");
		System.out.println("TIME IS: " + new Timestamp(date2.getTime()));

	}

	private String stemIt(String[] tokens) 
	{
		StringBuffer buffer = new StringBuffer();
		for (String token : tokens) 
		{
			stemmer.setCurrent(token);
			stemmer.stem();
			String text = stemmer.getCurrent();
			buffer.append(text);
			buffer.append(" ");
		}
		return buffer.toString().trim();
	}
	
	private String stemIt(String token) 
	{
		stemmer.setCurrent(token);
		stemmer.stem();
		String text = stemmer.getCurrent();
	
		return text ;
	}

	private void searchIndexes() throws IOException, ParseException {

		// Now search the index:
		String verb = "repudiates";
		// stemmer.setCurrent(verb);
		// stemmer.stem();
		// verb = stemmer.getCurrent() ;

		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		// Parse a simple query that searches for "text":
		QueryParser parser = new QueryParser(Version.LUCENE_CURRENT,
				TEXT_FIELD, analyzer);
		Query query = parser.parse(verb);
		ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		// assertEquals(1, hits.length);
		// Iterate through the results:
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			// assertEquals("This is the text to be indexed.",
			// hitDoc.get("fieldname"));
		}
		ireader.close();
		directory.close();
	}
	
	private void modifyIndexes () throws IOException
	{

		DirectoryReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_CURRENT, analyzer);

		IndexWriter indexWriter = new IndexWriter(directory, config);
		
		indexWriter.numDocs();
		
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
			
			writeVerb(verb);
			System.out.println("VERB PROCESSING FINISHED " + verb);
			java.util.Date date3 = new java.util.Date();
			System.out.println("TIME IS: " + new Timestamp(date3.getTime()));

		
		}

		ireader.close();
		directory.close();
	/*	
		//print the verbs now 
		String output = "./data/output/gigavector/" ;
		String outputFile = "pdtb_verb_vector_nostem_11142014_" ;
		BufferedWriter writer = null ;
		
		for ( String verb : contextCounter.keySet())
		{
			writer = new BufferedWriter ( new FileWriter ( output + "/" + outputFile + verb + ".txt")) ;
			writer.write("verb" + "\t" + "context" + "\t" + "count") ;
			writer.newLine() ;
		
			Counter<String> context = contextCounter.getCounter(verb) ;
			
			for ( String cont : context.keySet())
			{
				double count = context.getCount(cont) ;
				if ( count > 1.0 )
				{
					writer.write(verb + "\t" + cont + "\t" + count) ;
					writer.newLine() ;
				}
			}
			
			writer.close();
			System.out.println("VERB FINISHED " + verb);
			java.util.Date date3 = new java.util.Date();
			System.out.println("TIME IS: " + new Timestamp(date3.getTime()));

		}
		*/
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

	public static void main(String[] args) throws IOException, ParseException {
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

		IndexAndCreateWordVector indexSearchVectorObj = new IndexAndCreateWordVector();
		indexSearchVectorObj.activate(configFile);
		indexSearchVectorObj.loadPDTBVectors(pdtbPath, pdtbFile);
		indexSearchVectorObj.createIndex();
		indexSearchVectorObj.searchIndexesBySpan();
	}

}
