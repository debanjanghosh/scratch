package com.deft.sarcasm.train;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.deft.sarcasm.features.EXPERIMENT_MODE;
import com.sun.org.apache.xalan.internal.utils.FeatureManager.Feature;

import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.SVMLightClassifier;
import edu.stanford.nlp.classify.SVMLightClassifierFactory;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.Counter;

//import csli.util.classify.stanford.* ;

public class SarcasmTrainHandler {

	/**
	 * @param args
	 */
	
	public enum unigramTypeEnum
	{
		LOCAL, GLOBAL
	}
	
	
	private TextFileHandler trainingHandleObj ;
	private static String inputPath;
	private static String trainingFile;
	private String resourcePath;
	private String fileFormat;
	private String vocabUnigramFile;
	private String vocabBigramFile;
	
	private String nonLexFeatureFile ;
	
	private String outputPath;
	private String ngramPath;
	private String unigramType;
	private ArrayList<String> approvedFeatList;
	private String writingType;
	private String wekaPath;
	private String minFrequency;
	private String labelColumn;
	private String msgColumn;
	private String positiveCat;
	private String negativeCat;
	private String context;
	private String bowType;
	
	private static final String configPath =  "./data/config" ;
	
	public SarcasmTrainHandler() throws IOException
	{
		trainingHandleObj = new TextFileHandler(EXPERIMENT_MODE.TRAINING) ;
	}

	public void setResourceLoaderToTraining()
	{
//		trainingHandleObj.setResourceLoaderToTraining(resourceLoaderObj) ;
	}
	public void preProcess ( String path ) throws IOException
	{
		//one task is to load the dictionaries for LIWC
	//	resourceLoaderObj.loadLIWCDictionaries( path );
		trainingHandleObj.loadSelectiveParaphraseFile();
	}
	
	public void init () throws IOException, ClassNotFoundException
	{
		trainingHandleObj.setApprovedFeatureList(approvedFeatList);
		trainingHandleObj.setMinimumTokenFrequency(Integer.valueOf(minFrequency) );

		trainingHandleObj.setFileFormat(fileFormat);
		if (bowType.equalsIgnoreCase(unigramTypeEnum.LOCAL.toString()))
		{
			trainingHandleObj.setNgramFileType(unigramTypeEnum.LOCAL) ;
		}
		else
		{
			trainingHandleObj.setNgramFileType(unigramTypeEnum.GLOBAL) ;

		}
		
		trainingHandleObj.setUnigramFile(vocabUnigramFile,ngramPath,EXPERIMENT_MODE.TRAINING);
		trainingHandleObj.setBigramFile(vocabBigramFile,ngramPath,EXPERIMENT_MODE.TRAINING);
		
		trainingHandleObj.loadNGrams(EXPERIMENT_MODE.TRAINING);
		
		trainingHandleObj.setNonLexFeatureFile(nonLexFeatureFile,ngramPath);
		trainingHandleObj.setResourcePath(resourcePath);

		trainingHandleObj.setWritingType(writingType);
		trainingHandleObj.setWekaPath(wekaPath);
		
		trainingHandleObj.setLabelColumn(labelColumn) ;
		trainingHandleObj.setMsgColumn(msgColumn);

		trainingHandleObj.setPositiveCat(positiveCat);
		trainingHandleObj.setNegativeCat(negativeCat) ;
		
	}
	
	
	public void training (  ) throws IOException, ClassNotFoundException
	{
		
		if (context == null)
		{
			trainingHandleObj.createFeaturesForTraining(inputPath, outputPath,trainingFile) ;
		}
		else if (Integer.valueOf(context) ==0)
		{
			trainingHandleObj.createFeaturesForTraining(inputPath, outputPath,trainingFile) ;
		}
		else
		{
			trainingHandleObj.createFeaturesForContextTraining(inputPath, outputPath,trainingFile,Integer.valueOf(context)) ;
		}
		
		
	}
	
	public void activate ( String configFile) throws IOException
	{
		Properties prop = new Properties();
		InputStream input = null;
	 		
		input = new FileInputStream(configPath + "/" +configFile);
	 
		// load a properties file
		prop.load(input);
	 
		// get the property value and print it out
		inputPath = prop.getProperty("inputPathTrain");
		outputPath = prop.getProperty("outputPath");
		trainingFile = prop.getProperty("trainingFile");
		resourcePath = prop.getProperty("resourcePath");
		fileFormat = prop.getProperty("fileFormat") ;
		vocabUnigramFile = prop.getProperty("globalUnigramFile") ;
		vocabBigramFile = prop.getProperty("globalBigramFile") ;
		
		nonLexFeatureFile = prop.getProperty("nonLexFeatureFile") ;
		unigramType=prop.getProperty("unigramType");
		
		ngramPath = prop.getProperty("ngramPath") ;
		
		String features = prop.getProperty("features");
		approvedFeatList = new ArrayList<String>(Arrays.asList(features.split(",")));
		writingType =  prop.getProperty("writingType") ;
		wekaPath = prop.getProperty("wekaPath") ;
		minFrequency = prop.getProperty("minFrequency") ;
		labelColumn = prop.getProperty("labelColumn") ;
		msgColumn = prop.getProperty("msgColumn") ;
		
		positiveCat = prop.getProperty("positiveCat") ;
		negativeCat = prop.getProperty("negativeCat") ;
		
		context = prop.getProperty("ContextUse") ;
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
		
		
		SarcasmTrainHandler sarcasmHandlerObj = new SarcasmTrainHandler();
		
		System.out.println("TRAINING PROCEDURE FOR SARCASM DETECTION STARTED...") ;
		
		sarcasmHandlerObj.activate(configFile);
		sarcasmHandlerObj.init();
		
		
		//preprocess, e.g. load the dictionaries/paraphrases
	//	sarcasmHandlerObj.preProcess(resourceParth);
	
		//load the training/testing files - for classification
		sarcasmHandlerObj.training();
		
			
	}

}
