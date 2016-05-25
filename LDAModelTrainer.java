package com.research.course.debate.LDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.research.course.debate.preproc.PrepareDebateDataFilePerTopic;
import com.research.course.debate.util.DictionaryReaderFactoryBean;
import com.research.course.debate.util.KrovetzStemmer;
import com.research.course.debate.util.NonAlphaStopTokenizerFactory;
import com.research.course.debate.util.StemTokenizerFactory;

public class LDAModelTrainer {

	private TokenizerFactory tokenizerFactory;
	private File inputDirectory;
	private File outputDirectory;
	private String fileExtn ;

	public LDAModelTrainer(TokenizerFactory tokenizerFactory,
			File inputDirectory, File outputDirectory, String fileExtn) 
	{
		setTokenizerFactory(tokenizerFactory);
		setInputDirectory(inputDirectory);
		setOutputDirectory(outputDirectory);
		setFileExtn(fileExtn);

	}

	public void trainModel() throws IOException {
		File outputFile = new File(outputDirectory, "report." + fileExtn + ".txt");
		File modelFile = new File(outputDirectory, "lda_model." + fileExtn + ".bin");
		File stFile = new File(outputDirectory, "lda_symbols." + fileExtn + ".bin");

		int minTokenCount = LDAGenerator.MINIMUM_TOKEN_COUNT;
		short numTopics = 60;
		double topicPrior = 0.01; //1/100
		double wordPrior = 0.001; 

		int burninEpochs = 10;
		int sampleLag = 1;
		int numSamples = 200;
		long randomSeed = LDAGenerator.RANDOM_SEED;

		System.out.println("Minimum token count=" + minTokenCount);
		System.out.println("Number of topics=" + numTopics);
		System.out.println("Topic prior in docs=" + topicPrior);
		System.out.println("Word prior in topics=" + wordPrior);
		System.out.println("Burnin epochs=" + burninEpochs);
		System.out.println("Sample lag=" + sampleLag);
		System.out.println("Number of samples=" + numSamples);

		SymbolTable symbolTable = new MapSymbolTable();
		int[][] docTokens;
		int numTokens = 0;

		CharSequence[] articleTexts = LDADataUtil.readLdaInput(inputDirectory);
		docTokens = LatentDirichletAllocation.tokenizeDocuments(articleTexts,
				tokenizerFactory, symbolTable, minTokenCount);
		
		for (int[] tokens : docTokens) 
		{
			numTokens += tokens.length;
		}

		System.out.println("Number of unique words above count threshold="
				+ symbolTable.numSymbols());

		System.out.println("Tokenized.  #Tokens After Pruning=" + numTokens);
		 AbstractExternalizable.serializeTo((Serializable) symbolTable,
		 stFile);

		LDAReportingHandler handler = new LDAReportingHandler(symbolTable);

		LatentDirichletAllocation.GibbsSample sample = LatentDirichletAllocation
				.gibbsSampler(docTokens, numTopics, topicPrior, wordPrior,
						burninEpochs, sampleLag, numSamples, new Random(
								randomSeed), handler);

		LatentDirichletAllocation lda = sample.lda();
		AbstractExternalizable.serializeTo(lda, modelFile);

		int maxWordsPerTopic = 150;
		int maxTopicsPerDoc = 20;
		boolean reportTokens = true;
		handler.writeReport(sample, maxWordsPerTopic, maxTopicsPerDoc,
				outputFile, reportTokens);
	}

	public void setTokenizerFactory(TokenizerFactory tokenizerFactory) {
		this.tokenizerFactory = tokenizerFactory;
	}

	public void setInputDirectory(File inputDirectory) {
		checkDirectory(inputDirectory);
		this.inputDirectory = inputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) 
	{
		checkDirectory(outputDirectory);
		this.outputDirectory = outputDirectory;
	}

	public void setFileExtn ( String fileExtn)
	{
		this.fileExtn = fileExtn ;
	}
	
	
	private void checkDirectory(File directory) {
		if (directory.exists() && !directory.isDirectory()) {
			throw new IllegalArgumentException(directory
					+ " exists but it is not a directory");
		}

		if (!directory.exists() && !directory.mkdirs()) {
			throw new IllegalArgumentException(directory
					+ " does not exist and could not be created");
		}
	}


	public static void main(String[] args) throws Exception 
	{
		if (args.length < 2) 
		{
			System.err
					.println("Usage: LDAModelTrainer <inputDir> <outputDir> <factoryClass>");
			System.err
					.println("\tAll files found in the input directory will be used for training");
			System.err
					.println("\tTwo model files will be created in the output directory.  Existing files will be overwritten");
			System.err
					.println("\tThe factory class should be an implementation of IRecDocumentFactory that supports parsing the input files");
			System.exit(1);
		}

		// ClassPathXmlApplicationContext ctx = new
		// ClassPathXmlApplicationContext("classpath*:/lda-spring-config.xml");
		// final TokenizerFactory tokenizerFactory =
		// NonAlphaStopTokenizerFactory.INSTANCE;
		// ;//ctx.getBean("tokenizerFactory", TokenizerFactory.class);

		final TokenizerFactory tokenizerFactory = createTokenizer();// ctx.getBean("tokenizerFactory",
																	// TokenizerFactory.class);

		File inputDir = new File(args[0]);
		File outputDir = new File(args[1]);
		String fileExtn = args[2] ;

		@SuppressWarnings("unchecked")
		// Class<? extends IRecDocumentFactory> factoryClass = (Class<? extends
		// IRecDocumentFactory>) Class.forName(args[2]);
		// IRecDocumentFactory docFactory = factoryClass.newInstance();
		LDAModelTrainer ldaModelTrainer = new LDAModelTrainer(tokenizerFactory,
				inputDir, outputDir, fileExtn);
		ldaModelTrainer.trainModel();
	}

	private static TokenizerFactory createTokenizer() throws Exception {
		java.util.Set<String> stops = loadStopWords();
		// TS
		TokenizerFactory factory = new RegExTokenizerFactory(
				"[\\x2Da-zA-Z0-9]+");

		// TS
		factory = new NonAlphaStopTokenizerFactory(factory);

		// TS
		factory = new LowerCaseTokenizerFactory(factory);

		// TS
		factory = new EnglishStopTokenizerFactory(factory);

		// TS
		factory = new StopTokenizerFactory(factory, stops);

		 // TS iff KStemmer is new
		List<File> headwordFiles = listHeadwordFiles() ; 
		List<File> conflationsFiles = listConflationsFiles();
		DictionaryReaderFactoryBean dictBean = new DictionaryReaderFactoryBean(headwordFiles,conflationsFiles) ;
		
		
        KrovetzStemmer stemmer = new KrovetzStemmer();
        stemmer.setDictionary(dictBean.getDictionary());
        factory = new StemTokenizerFactory(factory, stemmer);
        
		return factory;

	}

	private static List<File> listConflationsFiles() {
		// TODO Auto-generated method stub
		List<File> conflationsFiles = new ArrayList<File>();
		conflationsFiles.add(new File ( "./data/config/kstem/country_nationality.txt")) ;
		conflationsFiles.add(new File ( "./data/config/kstem/direct_conflations.txt")) ;
		conflationsFiles.add(new File ( "./data/config/kstem/tlr_conflations.txt")) ;
		conflationsFiles.add(new File ( "./data/config/kstem/novus_conflations.txt")) ;
		return conflationsFiles;
	}

	private static List<File> listHeadwordFiles() 
	{
		// TODO Auto-generated method stub
		List<File> headwordFiles = new ArrayList<File>();
		headwordFiles.add(new File ( "./data/config/kstem/dict_supplement.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/head_word_list.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/exception_words.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/proper_nouns.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/tlr_dict_supplement.txt")) ;
		headwordFiles.add(new File ( "./data/config/kstem/novus_dict_supplement.txt")) ;
		
		return headwordFiles;
	}

	private static Set<String> loadStopWords() throws IOException {
		// TODO Auto-generated method stub
		
		String stopFile = "./config/lda/stopWordsDebate.lst" ;
		
		Set<String> stops = new HashSet<String>() ;
		
    	BufferedReader reader = new BufferedReader ( new FileReader ( stopFile)) ;
    	
    	while ( true )
    	{
    		String line = reader.readLine();
    		if ( null == line )
    		{
    			break ;
    		}
    		stops.add(line.trim());
    		
    	}
    	reader.close();
		return stops;
	}

}
