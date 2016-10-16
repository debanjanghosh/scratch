package com.deft.sarcasm.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.deft.sarcasm.features.EXPERIMENT_MODE;
import com.deft.sarcasm.train.TextFileHandler;
import com.deft.sarcasm.train.SarcasmTrainHandler.unigramTypeEnum;

import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.SVMLightClassifier;
import edu.stanford.nlp.classify.SVMLightClassifierFactory;

public class SarcasmTestHandler 
{
	private TextFileHandler testingHandleObj ;
	private static String inputPath;
	private String resourcePath;
	private String fileFormat;
	private String unigramFile;
	private String outputPath;
	private String ngramPath;
	private String localUnigramFile;
	private String globalUnigramFile;
	private String unigramType;
	private String vocabUnigramFile;
	private String vocabBigramFile;
	
	private String nonLexFeatureFile;
	private ArrayList<String> approvedFeatList;
	private String writingType;
	private String wekaPath;
	private String minFrequency;
	private String labelColumn;
	private String msgColumn;
	private String positiveCat;
	private String negativeCat;
	private String bowType;
	private static String testingFile;
	
	private static String configPath = "./data/config/" ;

	public SarcasmTestHandler() throws IOException
	{
		testingHandleObj = new TextFileHandler(EXPERIMENT_MODE.TESTING) ;
	}
	
	public void testing ( ) throws IOException, ClassNotFoundException
	{
		testingHandleObj.createFeaturesForTesting(inputPath, outputPath,testingFile) ;
	//	trainingHandleObj.writeWekaArffFile(path, testingFile);
	
	}
	
	public void preProcess ( String path ) throws IOException
	{
	//	testingHandleObj.loadAllParaphraseFile();
	}
	
	public void init() throws IOException
	{
		testingHandleObj.setApprovedFeatureList(approvedFeatList);
		testingHandleObj.setMinimumTokenFrequency(Integer.valueOf(minFrequency) );
		
		
		if (bowType.equalsIgnoreCase(unigramTypeEnum.LOCAL.toString()))
		{
			testingHandleObj.setNgramFileType(unigramTypeEnum.LOCAL) ;
		}
		else
		{
			testingHandleObj.setNgramFileType(unigramTypeEnum.GLOBAL) ;

		}



		testingHandleObj.setFileFormat(fileFormat);
//		trainingHandleObj.setUnigramFileType(unigramTypeEnum.GLOBAL) ;
		testingHandleObj.setUnigramFile(vocabUnigramFile,ngramPath,EXPERIMENT_MODE.TESTING);
		testingHandleObj.setBigramFile(vocabBigramFile,ngramPath,EXPERIMENT_MODE.TESTING);
		testingHandleObj.loadNGrams(EXPERIMENT_MODE.TESTING);

		testingHandleObj.setNonLexFeatureFile(nonLexFeatureFile,ngramPath);
		
	
		
		
		testingHandleObj.setResourcePath(resourcePath);
		testingHandleObj.setWritingType(writingType);
		testingHandleObj.setWekaPath(wekaPath);
		
		testingHandleObj.setLabelColumn(labelColumn) ;
		testingHandleObj.setMsgColumn(msgColumn);

		testingHandleObj.setPositiveCat(positiveCat);
		testingHandleObj.setNegativeCat(negativeCat) ;

		
	}
	
	public void activate ( String configFile) throws IOException
	{
		Properties prop = new Properties();
		InputStream input = null;
	 		
		input = new FileInputStream(configPath + "/" + configFile);
	 
		// load a properties file
		prop.load(input);
	 
		// get the property value and print it out
		inputPath = prop.getProperty("inputPathTest");
		outputPath = prop.getProperty("outputPath");
		
		testingFile = prop.getProperty("testingFile");
		resourcePath = prop.getProperty("resourcePath");
		fileFormat = prop.getProperty("fileFormat") ;
		ngramPath = prop.getProperty("ngramPath") ;
		vocabUnigramFile = prop.getProperty("globalUnigramFile") ;
		vocabBigramFile = prop.getProperty("globalBigramFile") ;
		nonLexFeatureFile = prop.getProperty("nonLexFeatureFile") ;
		unigramType=prop.getProperty("unigramType");
	
		String features = prop.getProperty("features");
		approvedFeatList = new ArrayList<String>(Arrays.asList(features.split(",")));
		writingType =  prop.getProperty("writingType") ;
		wekaPath = prop.getProperty("wekaPath") ;
		minFrequency = prop.getProperty("minFrequency") ;

		labelColumn = prop.getProperty("labelColumn") ;
		msgColumn = prop.getProperty("msgColumn") ;
	
		positiveCat = prop.getProperty("positiveCat") ;
		negativeCat = prop.getProperty("negativeCat") ;
	
		bowType = prop.getProperty("bowType") ;

		input.close() ;
	}

	
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		// TODO Auto-generated method stub
		if ( args.length !=2)
		{
			System.out.println("not enough parameters - please provide the config file") ;
			return ;
		}
		if (! args[0].equalsIgnoreCase("-c"))
		{
			System.out.println("not enough parameters - please provide the config file") ;
			return ;
		
		}
		
		String configFile = args[1] ;
		
	//	String resourcePath = "./data/twitter_corpus/dictionary/" ;
	//	String testingPath = "./data/twitter_corpus/" ;
	//	String testingPath = "./data/Spanish_Data/" ;
	//	String testingFile = "sarcasm.positive.negative.02132014.filtered.shuffled.test" ;
		
	//	String testingFile = "raw_human_judges_testingaggr_s_p_n.txt" ;
		//sarthaks data
	//	String testingFile = "TestData.tweet.txt" ;
	//	testingFile = "rawcorpus_with_human_coders_modified_test_S_P_N.txt" ;
	//	testingFile = "spanish.random.spn.03012014.testing" ;
		
	//	testingFile = "tweet.sarcasm.random.3000.bbn.filtered.shuffled" ;
		
		SarcasmTestHandler sarcasmHandlerObj = new SarcasmTestHandler() ;
		
		System.out.println("TESTING/EVALUATION PROCEDURE FOR SARCASM DETECTION STARTED...") ;
		
		
		sarcasmHandlerObj.activate(configFile);
		
	//	sarcasmHandlerObj.preProcess(resourcePath);
		
		sarcasmHandlerObj.init();
		
		sarcasmHandlerObj.testing();
		
		//check the original ones with 2700 lines and compare the performance with this

	}
	
}
