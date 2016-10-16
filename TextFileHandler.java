package com.deft.sarcasm.train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.lang3.StringUtils;

import com.deft.sarcasm.features.BoWFeatureLoader;
import com.deft.sarcasm.features.EXPERIMENT_MODE;
import com.deft.sarcasm.features.LexicalPragFeatureLoader;
import com.deft.sarcasm.features.MPQAFeatureGenerator;
import com.deft.sarcasm.features.NonLexFeatureHandler;
import com.deft.sarcasm.features.PunctFeatureLoader;
import com.deft.sarcasm.features.WekaWriter;
import com.deft.sarcasm.train.SarcasmTrainHandler.unigramTypeEnum;
import com.deft.sarcasm.util.TextUtility;

//stanford classes 
import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.SVMLightClassifierFactory;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.RVFDatum;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator ;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class TextFileHandler 
{
	private HashMap<String, List<Map<Integer,Double>>> allFeatureMap;

	private SarcasmResourceLoader sarcasmRLObject;

	private BoWFeatureLoader bowFeatureObj;

	private LexicalPragFeatureLoader lexFeatureObj;

	private PunctFeatureLoader punctFeatureObj;
	
	private String fileFormat;

	private unigramTypeEnum unigramTypeEnum;

	private TokenizerModel model;

	private TokenizerME tokenizer;

	private FileInputStream nlpStream;

	private ArrayList<String> approvedFeatList;

	private WekaWriter wekaWriterObj;

	private String writingType;

	private String wekaPath;

	private Integer labelColumn;

	private Integer msgColumn;

	private ArrayList<String> positiveCat;

	private ArrayList<String> negativeCat;

	private MPQAFeatureGenerator sentimentObj;
	
	private static final String POSITIVE_CAT = "1" ;
	private static final String NEGATIVE_CAT = "0" ;
	
	
	private enum FEATURE_TYPE
	{
		BINARY, COUNT
	}
	
	private enum FILE_FORMAT
	{
		SGML, TEXT
	}
	
	private enum FEATURE_SET
	{
		BOW, LIWC, POLARITY, PERIODS, ALTSPELL,WP,SENT
	}
	
	private static final String EOL = "\n" ;

	private static Pattern ldcPattern = Pattern.compile("<DOC");
	private static Pattern xmlParagraphPattern = Pattern.compile("<P\\s+sarcasm=\"(yes|no)\"\\s+pid=\"([0-9]+)\">([^<]+)");

	private static final String OPENNLP_TOKENFILE = "en-token.bin" ;
	
	public TextFileHandler(EXPERIMENT_MODE experMode) throws IOException 
	{
		sarcasmRLObject = new SarcasmResourceLoader(experMode) ;
		punctFeatureObj = new PunctFeatureLoader() ;
		bowFeatureObj = new BoWFeatureLoader(experMode,sarcasmRLObject) ;
	//	new NonLexFeatureHandler(experMode,sarcasmRLObject);
		
		lexFeatureObj = new LexicalPragFeatureLoader(sarcasmRLObject) ;
		
		nlpStream = new FileInputStream("./data/config/" + OPENNLP_TOKENFILE );
	
		sentimentObj = new MPQAFeatureGenerator () ;
	
	}
	
	

	/*
	public void loadAllParaphraseFile () throws IOException
	{
		String path = "/Users/dg513/work/eclipse-workspace/nyucourse-workspace/NYUCourse/data/project/eval/" ;
		
		File file = new File ( path);
		
		File files[] = file.listFiles() ;
		
		BufferedReader reader = null ;
		
		for ( File f : files )
		{
			//moses op
			if (f.getName().contains("phrase-table-moses-oppp-01052014.txt.only.OP.phrases")  )
		//	if ( (f.getName().contains("ibm2_op_AMTResults_vote.txt")) || (f.getName().contains("moses_op_AMTResults_vote.txt")) )
			{
				reader = new BufferedReader ( new FileReader ( path + "/" + f.getName())) ;
				String header = reader.readLine() ;
				
				while ( true )
				{
					String line = reader.readLine() ;
					if ( null == line )
					{
						break;
					}
					//0	1	<i willingly went to>	<i was not conned into going to:0.375>
					if (line.isEmpty())
					{
						continue ;
					}
					String features[] = line.split("\t") ;
					
					
					
					String sarcasm = features[3] ;
					sarcasm = sarcasm.substring(1,sarcasm.length()-1);
					
					String msg = sarcasm.split(":")[0];
					double probScore = Double.valueOf(sarcasm.split(":")[1]);
				
					if(! (probScore>0.75) )
					{
						continue ;
					}
				
					features = msg.split("\\s++") ;
					
					for ( int i = 0 ; i < features.length ; i++ )
					{
						for ( int j = i+1 ; j < features.length ; j++ )
						{
							String bigram = features[i] + "|||" + features[j] ;
							bigram = bigram.toLowerCase();
							if (!bigramList.contains(bigram) )
							{
								bigramList.add(bigram);
							}
						}
					}
					
				}
				
				reader.close();
			}
		}
	}
*/	
	public void loadSelectiveParaphraseFile () throws IOException
	{
		bowFeatureObj.loadSelectiveParaphraseFile();
	}
	
	
	
	//experiments on context/sarcasm - making it a different function since 
	//the data, parsing etc. will be little different
	
	
	public void createFeaturesForContextTraining(String inputPath, String outputPath,String trainingFile, int context)
			throws IOException, ClassNotFoundException 
	{
	/*	
		String t = "John Bauer works at Stanford." ;
		MaxentTagger maxentTagger =  new MaxentTagger("./models/tagger/english-left3words-distsim.tagger");
		POSTaggerAnnotator tagger =  new POSTaggerAnnotator (maxentTagger); 
		Annotation ann = new Annotation(t);
		
		String tagged = maxentTagger.tagString(t);
		System.out.println(tagged);
		
		tagger.annotate(ann);
	*/
		sentimentObj.init(); 
		
	//	createSentimentContextFeatures(inputPath,outputPath,trainingFile,EXPERIMENT_MODE.TRAINING, FEATURE_TYPE.BINARY,context) ;

		createContextFeatures(inputPath,outputPath,trainingFile,EXPERIMENT_MODE.TRAINING, FEATURE_TYPE.BINARY,context) ;
	//	writeWekaArffFile(path,trainingFile,FEATURE_TYPE.BINARY);
	}
	
	
	
	
	
	public void createFeaturesForTraining(String inputPath, String outputPath,String trainingFile)
			throws IOException, ClassNotFoundException 
	{
	/*	
		String t = "John Bauer works at Stanford." ;
		MaxentTagger maxentTagger =  new MaxentTagger("./models/tagger/english-left3words-distsim.tagger");
		POSTaggerAnnotator tagger =  new POSTaggerAnnotator (maxentTagger); 
		Annotation ann = new Annotation(t);
		
		String tagged = maxentTagger.tagString(t);
		System.out.println(tagged);
		
		tagger.annotate(ann);
	*/

		createFeatures(inputPath,outputPath,trainingFile,EXPERIMENT_MODE.TRAINING, FEATURE_TYPE.BINARY) ;
	//	writeWekaArffFile(path,trainingFile,FEATURE_TYPE.BINARY);
	}
	
	public void createSentimentContextFeatures ( String inputPath, String outputPath, String trainingFile,
			EXPERIMENT_MODE experMode, FEATURE_TYPE featType, int context)
			throws IOException, ClassNotFoundException 
	{
		
		TextUtility.loadHashtags();
		
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputPath  + "/" + trainingFile), "UTF8"));
	
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputPath + "/" + trainingFile + ".current.binary.svm." 
						+ experMode.toString() + "." + "txt.temp"),
				"UTF8"));
		
		System.out.println("PROCESSING: " + trainingFile) ;
		
		int lineNumber = 1;
		
		allFeatureMap = new HashMap<String,List<Map<Integer,Double>>>() ;
		
		model = new TokenizerModel(nlpStream);
		 
		tokenizer = new TokenizerME(model);
		
		while (true) 
		{
			String line = reader.readLine();
			if (null == line) 
			{
				break;
			}
			
//			if ( lineNumber == 435)
//			{
//				System.out.println("here") ;
//			}

			String features[] = line.split("\t") ;
			String label = features[labelColumn];//getLabel(trainingFile);
			label = convert(label) ;
			
		//	String target = features[1].trim();
		//	String tweetId = features[2].trim();
			line = features[msgColumn].trim();
			line = StringUtils.stripAccents(line);
			
			String messages[] = line.split("\\|\\|\\|") ;
			String msg = null ;
			String prev_msg = null ;
			if (messages.length == 2)
			{
				//context 
				msg = messages[0] ;
				prev_msg = messages[1] ;
	//			msg = prev_msg + " " + msg ; //no context means commented out 
			}
			else
			{
				System.out.println(" number of lines are more? " + line) ;
				continue ;
			}
			
			msg = TextUtility.removeHashtags(msg);
			String tokens1[] =  tokenizer.tokenize(msg.trim()) ;
			
			prev_msg = TextUtility.removeHashtags(prev_msg);
			String tokens2[] =  tokenizer.tokenize(prev_msg.trim()) ;

			
			Map<Integer, Double> lexPragFeatMap = null ;

			TreeMap<Integer,Double> allFeatures = new TreeMap<Integer,Double>() ;

			Map<String,Double> sentiMap1 = sentimentObj.extractSentiFeatures(Arrays.asList(tokens1));
			Map<String,Double> sentiMap2 = sentimentObj.extractSentiFeatures(Arrays.asList(tokens2));

				
		//	if (! sentiMap1.isEmpty())
			{
			//	if ( label == "1" )
				{
					String ret = label + "\t" + TextUtility.getValuesOfMap(sentiMap1);//,sentiMap2) ;
					writer.write(ret);// + " " + "#" + label + "-" + tweetId );
					writer.newLine();

				}
	//		lexPragFeatMap = sarcasmRLObject.createNonNgramFeatures(sentiMap1) ;
	//			allFeatures.putAll(lexPragFeatMap);
				
			}
		//	writer.write(label + "\t" + lexPragFeatMap.toString());// + " " + "#" + label + "-" + tweetId );
		//	writer.newLine();

			if ((lineNumber % 100) == 0) 
			{
//				System.out.println("Sentences done " + lineNumber);
			}
			lineNumber++;
			
		}

		writer.close();
		System.out.println("Sentences done " + (lineNumber));
		
				
	}
	
	public void createContextFeatures ( String inputPath, String outputPath, String trainingFile,
			EXPERIMENT_MODE experMode, FEATURE_TYPE featType, int context)
			throws IOException, ClassNotFoundException 
	{
		
		TextUtility.loadHashtags();
		
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputPath  + "/" + trainingFile), "UTF8"));
	
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputPath + "/" + trainingFile + ".current.svm." 
						+ experMode.toString() + "." + "txt"),
				"UTF8"));
		
		
		
		System.out.println("PROCESSING: " + trainingFile) ;
		
		int lineNumber = 1;
		
		allFeatureMap = new HashMap<String,List<Map<Integer,Double>>>() ;
		
		model = new TokenizerModel(nlpStream);
		 
		tokenizer = new TokenizerME(model);
		
		Set<String> uniques = new HashSet<String>() ;
		
		while (true) 
		{
			String line = reader.readLine();
			if (null == line) 
			{
				break;
			}
			
	//		if ( lineNumber == 435)
		//	{
			//	System.out.println("here") ;
			//}
			
			//check if the format is sgml or not
			if(fileFormat.equalsIgnoreCase(FILE_FORMAT.SGML.toString()))
			{
				//we need to "parse" the line
				//first check if the lines contain and preamble or postambles
				if(line.contains("<TEXT>") || line.contains("</TEXT>") || line.contains("</DOC>") ||
						line.contains("<DOC>"))
				{
					continue ;
				}
				
				//else use the reg expr 
				line = getXMLCleanText(line) ;
			}
			
			String features[] = line.split("\t") ;
			String label = features[labelColumn];//getLabel(trainingFile);
			label = convert(label) ;
			
		//	String target = features[1].trim();
		//	String tweetId = features[2].trim();
			line = features[msgColumn].trim();
			line = StringUtils.stripAccents(line);
			
			String messages[] = line.split("\\|\\|\\|") ;
			String msg = null ;
			String prev_msg = null ;
			if (messages.length == 2)
			{
				//context 
				msg = messages[0] ;
				prev_msg = messages[1] ;
			}
			else if (messages.length == 1)
			{
				msg = messages[0] ;
				System.out.println ("msg length is 1 - check ") ;
				continue ; //we are only dealing where we have the previous context....
			}
			else
			{
				System.out.println(" number of lines are more? " + line) ;
				continue ;
			}
			
			msg = TextUtility.removeHashtags(msg);
			prev_msg = TextUtility.removeHashtags(prev_msg);
			String tokens1[] =  tokenizer.tokenize(msg.trim()) ;
			String tokens2[] =  tokenizer.tokenize(prev_msg.trim()) ;
			
			context =  0 ;
			String tokens_all[] = null ;
			String all_msg = null ;
			if ( context == 1) //that is both! 
			{
				all_msg = msg + " " + prev_msg ;
				tokens_all =  tokenizer.tokenize(all_msg.trim()) ;
			}
			
			//this is only for the current wsd experiment
			List<String> hashes = new ArrayList<String>() ;
			
			
			Map<Integer, Double> puncFeatMap = null ;
			Map<Integer, Double> bowFeatMap = null ;
			Map<Integer, Double> lexPragFeatMap = null ;
			TreeMap<Integer,Double> allFeatures = new TreeMap<Integer,Double>() ;
			
			if(approvedFeatList.contains(FEATURE_SET.PERIODS.toString()) )
			{
				
				Map<String,Double> puncMap = punctFeatureObj.getPuncFVs(msg);
				if (!puncMap.isEmpty() ) 
				{
					puncFeatMap = sarcasmRLObject.createNonNgramFeatures(puncMap) ;
					allFeatures.putAll(puncFeatMap) ;

				}
			}
			
			if(approvedFeatList.contains(FEATURE_SET.BOW.toString() ) )
			{
			
				bowFeatMap = bowFeatureObj.loadFeatures(tokens1);
				allFeatures.putAll(bowFeatMap);
				
			}
			
			if(approvedFeatList.contains(FEATURE_SET.WP.toString() ) )
			{
			
				bowFeatMap = bowFeatureObj.generateBigrams(tokens1);
				allFeatures.putAll(bowFeatMap);
				
			}
			
			if(approvedFeatList.contains(FEATURE_SET.LIWC.toString()) )
			{
				Map<String,Double> lexPragMap = lexFeatureObj.loadNonLexFeatures(tokens1);
				
				if (! lexPragMap.isEmpty())
				{
					lexPragFeatMap = sarcasmRLObject.createNonNgramFeatures(lexPragMap) ;
					allFeatures.putAll(lexPragFeatMap);
					
				}
			}
			
			if(approvedFeatList.contains(FEATURE_SET.SENT.toString()) )
			{
				Map<String,Double> sentiMap = null ;
				Map<String,Double> lexPragMap = null ;
				if ( context == 1 )
				{
					sentiMap = sentimentObj.extractSentiFeatures(Arrays.asList(tokens1),Arrays.asList(tokens2));
					lexPragMap = TextUtility.extractSentiFeaturesAsMap(sentiMap,context); 
				}
				else if ( context == 0 )
				{
					sentiMap = sentimentObj.extractSentiFeatures(Arrays.asList(tokens1));
					lexPragMap = TextUtility.extractSentiFeaturesAsMap(sentiMap,context);     

				}
				else
				{
					System.out.println("error in context setting?") ;
				}
				
				if (! lexPragMap.isEmpty())
				{
					lexPragFeatMap = sarcasmRLObject.createNonNgramFeatures(lexPragMap) ;
					allFeatures.putAll(lexPragFeatMap);
					
				}
			}

			
			
			String fv = createFV(allFeatures,featType);
			
			writer.write(label + "\t" + fv);// + " " + "#" + label + "-" + tweetId );
			writer.newLine();

			if ((lineNumber % 100) == 0) 
			{
				System.out.println("Sentences done " + lineNumber);
			}
			
	//		if ( lineNumber == 200)
	//		{
	//			break ;
	//		}
			lineNumber++;
			
		}

		writer.close();
		System.out.println("Sentences done " + (lineNumber));
		
		//for training purpose - we write the non unigram file
		//update - no we don't write because non-unigram can be used as a 
		//constant file
		//we rather write the bow file? so that feature_weight_calculation
		//becomes easier?
		if(experMode.equals(EXPERIMENT_MODE.TRAINING))
		{
		//	sarcasmRLObject.writeNonNgramFiles() ;
		
	//		sarcasmRLObject.writeNGramFile();
		}
		
		if(writingType.equals("weka") )
		{
			//write a weka file
			wekaWriterObj = new WekaWriter();
			List<String> nonLexFeatures = sarcasmRLObject.getNonLexFeatures();
			List<String> lexFeatures = sarcasmRLObject.getLexFeatures();
			
			List<String> allFeatures = new ArrayList<String>() ;
			allFeatures.addAll(lexFeatures);
			allFeatures.addAll(nonLexFeatures);
			
			wekaWriterObj.setFeatures(allFeatures);
			wekaWriterObj.setStartingPointForFeaturePostion(lexFeatures.size());
			wekaWriterObj.setLabels(allFeatureMap.keySet());
			wekaWriterObj.setFeatureMap(allFeatureMap);
			wekaWriterObj.writeWekaArffFile(wekaPath, trainingFile +".weka") ;
			
		
		}
				
		
		
		//maintain a global map for feature - value type where the value is meaningless
		///basically we want to keep an index of all the *features* so that during training
		//we can get back to feature indexing by checking which feature is which index 
		//and what is the value of the feature during training
		//this is just for testing and will be removed in production
	//	String specialFile = "unigram.checkIndex.lst" ;
	//	bowFeatureObj.close(specialFile) ;
		
		
	}
	
	public void createFeatures ( String inputPath, String outputPath, String trainingFile,
			EXPERIMENT_MODE experMode, FEATURE_TYPE featType)
			throws IOException, ClassNotFoundException 
	{
		
		TextUtility.loadHashtags();
	//	TextUtility.loadCrimeHashtags();
		
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputPath  + "/" + trainingFile), "UTF8"));
	
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputPath + "/" + trainingFile + ".binary.svm." 
						+ experMode.toString() + "." + "txt"),
				"UTF8"));
		
		System.out.println("PROCESSING: " + trainingFile) ;
		
		int lineNumber = 1;
		
		allFeatureMap = new HashMap<String,List<Map<Integer,Double>>>() ;
		
		model = new TokenizerModel(nlpStream);
		 
		tokenizer = new TokenizerME(model);
		
		Set<String> uniques = new HashSet<String>() ;
		
		while (true) 
		{
			String line = reader.readLine();
			if (null == line) 
			{
				break;
			}
			
			//check if the format is sgml or not
			if(fileFormat.equalsIgnoreCase(FILE_FORMAT.SGML.toString()))
			{
				//we need to "parse" the line
				//first check if the lines contain and preamble or postambles
				if(line.contains("<TEXT>") || line.contains("</TEXT>") || line.contains("</DOC>") ||
						line.contains("<DOC>"))
				{
					continue ;
				}
				
				//else use the reg expr 
				line = getXMLCleanText(line) ;
			}
			
			//sarthak-s data
		/*	
			String features[] = line.split("\\s") ;
			String label = features[0].trim();
			line = TextUtility.recreate(features,1,features.length);
			String tokens[] = line.trim().split("\\s++");
		*/	
		//	System.out.println(line);
			
			String features[] = line.split("\t") ;
			if ( features.length <2)
			{
				continue ;
			}
			String label = features[labelColumn];//getLabel(trainingFile);
			label = convert(label) ;
			
		//	String target = features[1].trim();
		//	String tweetId = features[2].trim();
			line = features[msgColumn].trim();
			line = StringUtils.stripAccents(line);
	//		line = line.toLowerCase();
		
			//we have some issues in tokenization
			//the easy solve is to replace alll hashtags
			
			//if the line contains both hashtags - remove that!
			
			line = TextUtility.removeHashtags(line);
			
			/*
			if(uniques.contains(line))
			{
				continue ;
			}
			uniques.add(line) ;
			*/
			String tokens[] =  tokenizer.tokenize(line.trim()) ;
	//		String tokens[] = line.split("\\s++");
			
			//check if sarcasm or sarcastic tokens are part of the text
	//		boolean presence = TextUtility.checkSarcasm(tokens);
			
//			if(presence)
//			{
//				continue ;
//			}
			
			//this is only for the current wsd experiment
			List<String> hashes = new ArrayList<String>() ;
			//for all targets
	//		loadAllHashes(hashes);
	//		bowFeatureObj.addDataToHashList(hashes) ;
		
			
			
			Map<Integer, Double> puncFeatMap = null ;
			Map<Integer, Double> bowFeatMap = null ;
			Map<Integer, Double> lexPragFeatMap = null ;
			TreeMap<Integer,Double> allFeatures = new TreeMap<Integer,Double>() ;
			
			if(approvedFeatList.contains(FEATURE_SET.PERIODS.toString()) )
			{
				Map<String,Double> puncMap = punctFeatureObj.getPuncFVs(line);
				if (!puncMap.isEmpty() ) 
				{
					puncFeatMap = sarcasmRLObject.createNonNgramFeatures(puncMap) ;
					allFeatures.putAll(puncFeatMap) ;

				}
			}
			
			if(approvedFeatList.contains(FEATURE_SET.BOW.toString() ) )
			{
			
				bowFeatMap = bowFeatureObj.loadFeatures(tokens);
				allFeatures.putAll(bowFeatMap);
				
			}
			
			
			if(approvedFeatList.contains(FEATURE_SET.WP.toString() ) )
			{
			
				bowFeatMap = bowFeatureObj.generateBigrams(tokens);
				allFeatures.putAll(bowFeatMap);
				
			}
			

			
			
			if(approvedFeatList.contains(FEATURE_SET.LIWC.toString()) )
			{
				Map<String,Double> lexPragMap = lexFeatureObj.loadNonLexFeatures(tokens);
				
				if (! lexPragMap.isEmpty())
				{
					lexPragFeatMap = sarcasmRLObject.createNonNgramFeatures(lexPragMap) ;
					allFeatures.putAll(lexPragFeatMap);
					
				}
			}
			
			//we will introduce a feature based on emoticons - but first check the 
			//training data to see the variations of the such emoticons
			
			
			//get upper cases (e.g. NEVER) / weird spelling of words (e.g. Coooool/)
			
			
			
	
	//		Map<Integer, Integer> bigramMap = bowFeatureObj.generateBigram(tokens);
			
			// create the feature vector
		
			//dummy value at position 1
		//	allFeatures.put(0, 1);
			
			
		//	allFeatures.putAll(bigramMap);
	
			String fv = createFV(allFeatures,featType);
			
		/*	
			List<Map<Integer, Double>>  fvs = allFeatureMap.get(label);

			if ( null == fvs )
			{
				fvs = new ArrayList<Map<Integer,Double>>();
			}
			label = convert(label);
			fvs.add(allFeatures);
			allFeatureMap.put(label, fvs);
		*/	
			if (label.equalsIgnoreCase("pos"))
			{
				label = "1" ;
			}
			
			if (label.equalsIgnoreCase("neg"))
			{
				label = "0" ;
			}

			
			
			writer.write(label + "\t" + fv);// + " " + "#" + label + "-" + tweetId );
			writer.newLine();

			if ((lineNumber % 100) == 0) 
			{
				System.out.println("Sentences done " + lineNumber);
			}
			
	//		if ( lineNumber == 200)
	//		{
	//			break ;
	//		}
			lineNumber++;
			
		}

		writer.close();
		System.out.println("Sentences done " + (lineNumber));
		
		//for training purpose - we write the non unigram file
		//update - no we don't write because non-unigram can be used as a 
		//constant file
		//we rather write the bow file? so that feature_weight_calculation
		//becomes easier?
		if(experMode.equals(EXPERIMENT_MODE.TRAINING) &&
				this.unigramTypeEnum == unigramTypeEnum.LOCAL)
		{
		//	sarcasmRLObject.writeNonNgramFiles() ;
		
			//if the ngram is local type we need to write the output for testing...
			if(approvedFeatList.contains(FEATURE_SET.BOW.toString()) )
			{
				sarcasmRLObject.writeUnigramFile(); 
			}
		}
		
		if(writingType.equals("weka") )
		{
			//write a weka file
			wekaWriterObj = new WekaWriter();
			List<String> nonLexFeatures = sarcasmRLObject.getNonLexFeatures();
			List<String> lexFeatures = sarcasmRLObject.getLexFeatures();
			
			List<String> allFeatures = new ArrayList<String>() ;
			allFeatures.addAll(lexFeatures);
			allFeatures.addAll(nonLexFeatures);
			
			wekaWriterObj.setFeatures(allFeatures);
			wekaWriterObj.setStartingPointForFeaturePostion(lexFeatures.size());
			wekaWriterObj.setLabels(allFeatureMap.keySet());
			wekaWriterObj.setFeatureMap(allFeatureMap);
			wekaWriterObj.writeWekaArffFile(wekaPath, trainingFile +".weka") ;
			
		
		}
				
		
		
		//maintain a global map for feature - value type where the value is meaningless
		///basically we want to keep an index of all the *features* so that during training
		//we can get back to feature indexing by checking which feature is which index 
		//and what is the value of the feature during training
		//this is just for testing and will be removed in production
	//	String specialFile = "unigram.checkIndex.lst" ;
	//	bowFeatureObj.close(specialFile) ;
		
		
	}
	

	
	private void loadAllHashes(List<String> hashes) throws IOException 
	{
		// TODO Auto-generated method stub
		String data = "./data/config/";
		String file = "topnames.txt" ;
		List<String> targets = Files.readAllLines( Paths.get(data+file), StandardCharsets.UTF_8) ;
		
		for ( String target : targets )
		{
			hashes.add(target);
			hashes.add("#" +target);
		}
	
	}

	private String convert(String label) 
	{
		// TODO Auto-generated method stub
		label = label.trim();
		
		if ( positiveCat.contains(label))
		{
			return POSITIVE_CAT ;
		}
		
		else if ( negativeCat.contains(label))
		{
			return NEGATIVE_CAT ;
		}
		
		else
		{
			System.out.println("wrong label. check") ;
		}
		return null;
	}
	

	private static String labelConvert(String label) 
	{
		// TODO Auto-generated method stub
		if ( label.equalsIgnoreCase("yes"))
			return "1" ;
		else if ( label.equalsIgnoreCase("no"))
			return "2" ;
		
		return null;
	}

		private String createFV ( Map<Integer,Double> fvs, FEATURE_TYPE type )
	{
		
		StringBuffer ret = new StringBuffer();
		for ( Integer feature : fvs.keySet() )
		{
			ret.append(feature);
			ret.append(":") ;
			
			if(type.equals(FEATURE_TYPE.BINARY))
			{
				ret.append("1") ;
			}
			else if (type.equals(FEATURE_TYPE.COUNT))
			{
				ret.append(fvs.get(feature));
			}
			ret.append(" ") ;
			
		}
		
		return ret.toString().trim() ;
	}

	public void setResourceLoaderToTraining(SarcasmResourceLoader resourceObj) 
	{
	}

	public void createFeaturesForTesting(String inputPath, String outputPath, String testingFile) 
			throws ClassNotFoundException, IOException 
	{
		// TODO Auto-generated method stub
		createFeatures(inputPath, outputPath,testingFile, EXPERIMENT_MODE.TESTING, FEATURE_TYPE.BINARY) ;
	}
	
	
	
	public void setResourcePath(String resourcePath) throws IOException 
	{
		lexFeatureObj.setLexPath(resourcePath) ;

		
				
	}
	
	public void setNonLexFeatureFile(String nonLexFeatureFile,
			String unigramPath) throws IOException 
	{
		sarcasmRLObject.setNonLexfile(nonLexFeatureFile);
		
	//	if (experMode.equals(EXPERIMENT_MODE.TESTING) )
		{
			//we load the non-lex features for everythinge because it is constant!
			sarcasmRLObject.loadGlobalNonLexFeatures();
			
		}
		
		System.out.println("NON LEXICAL FEATURE INITIALIZATION FINISHED ");

	}
	
	
	
	
	public void setBigramFile(String bigramFile, String nramPath, EXPERIMENT_MODE mode) throws IOException 
	{
		bowFeatureObj.setBigramFile(bigramFile,nramPath) ;
		
		System.out.println("WP INITIALIZATION FINISHED ");

	}

	
	public void setUnigramFile(String unigramFile, String unigramPath, EXPERIMENT_MODE mode) throws IOException 
	{
		bowFeatureObj.setUnigramFile(unigramFile,unigramPath) ;
		
	}
	
	public void loadNGrams(EXPERIMENT_MODE mode) throws IOException
	{
		if(approvedFeatList.contains(FEATURE_SET.BOW.toString()))
		{
			if (mode.equals(EXPERIMENT_MODE.TRAINING) && 
					unigramTypeEnum.equals(unigramTypeEnum.GLOBAL))
			{
				bowFeatureObj.loadGlobalNgrams(FEATURE_SET.BOW.toString());
			}

			if (mode.equals(EXPERIMENT_MODE.TESTING) )
			{
				if ( unigramTypeEnum.equals(unigramTypeEnum.LOCAL))
				{
					bowFeatureObj.loadLocalNgrams(FEATURE_SET.BOW.toString());
				}
				if ( unigramTypeEnum.equals(unigramTypeEnum.GLOBAL))
				{
					bowFeatureObj.loadGlobalNgrams(FEATURE_SET.BOW.toString());
				}

			}
		}
		
		if(approvedFeatList.contains(FEATURE_SET.WP.toString()))
		{
			if (mode.equals(EXPERIMENT_MODE.TRAINING) && 
					unigramTypeEnum.equals(unigramTypeEnum.GLOBAL))
			{
				bowFeatureObj.loadGlobalNgrams(FEATURE_SET.WP.toString());
			}

			if (mode.equals(EXPERIMENT_MODE.TESTING) )
			{
				if (unigramTypeEnum.equals(unigramTypeEnum.GLOBAL))
				{
					bowFeatureObj.loadGlobalNgrams(FEATURE_SET.WP.toString());
				}
				if (unigramTypeEnum.equals(unigramTypeEnum.LOCAL))
				{
					bowFeatureObj.loadLocalNgrams(FEATURE_SET.WP.toString());
				}

			}
		}
		

		
		System.out.println("BOW INITIALIZATION FINISHED ");

	}

	public void setFileFormat(String fileFormat) 
	{
		// TODO Auto-generated method stub
		this.fileFormat = fileFormat ;
	}

	private static String getXMLCleanText(String documentString) 
	{
		Matcher m = xmlParagraphPattern.matcher(documentString);
		StringBuffer buffer = new StringBuffer() ;
		while(m.find())
		{
			
			String label = m.group(1).trim() ;
	//		System.out.println("label=" + " " + label);
			
			String snum = m.group(2).trim() ;
	//		System.out.println("snum=" + " " + snum);
		
			
			String text = m.group(3).trim() ;
			text = text.replaceAll("\\n", " ") ;
			text = text.replaceAll("\\r", " ");
			text = text.replaceAll("\t", " ");
		//	System.out.println("text=" + " " + text);
			
			//check surrogates
			text = checkSurrogates(text);
			text = text + " .";
			
			buffer.append(labelConvert(label));
			buffer.append("\t");
			buffer.append(snum);
			buffer.append("\t");
			buffer.append(text);
		}
		
	//	System.out.println("the size of the paragraphss is1 " + paragraphs.size()) ;
		
		return buffer.toString().trim();
	}
	
	private static String checkSurrogates (String text )
	{
		StringBuffer buffer = new StringBuffer() ;
		char[] chars = text.toCharArray() ;
		for ( Character c : chars )
		{
			if(Character.isHighSurrogate(c) || Character.isLowSurrogate(c) )
			{
	//			System.out.println("here");
			}
			else
			{
				buffer.append(c);
			}
		}
		
		return buffer.toString();
	}

	public void setNgramFileType(unigramTypeEnum type) 
	{
		// TODO Auto-generated method stub
		this.unigramTypeEnum = type ;
		sarcasmRLObject.setNgramFileType(type);
		bowFeatureObj.setNgramFileType(type);
	}

	public void setApprovedFeatureList(ArrayList<String> approvedFeatList) 
	{
		// TODO Auto-generated method stub
		this.approvedFeatList = approvedFeatList ;
		
		sarcasmRLObject.setNGramFeatureTypes(approvedFeatList) ;
	}

	public void setWritingType(String writingType) {
		// TODO Auto-generated method stub
		this.writingType = writingType ;
	}

	public void setWekaPath(String wekaPath) {
		// TODO Auto-generated method stub
		this.wekaPath = wekaPath ;
	}

	public void setMinimumTokenFrequency(int minFrequency) {
		if (minFrequency > 0)
		{
			sarcasmRLObject.setDFFilter(true);
			sarcasmRLObject.setMinimumDF(minFrequency);
		}
	}

	public void setLabelColumn(String labelColumn) 
	{
		// TODO Auto-generated method stub
		 this.labelColumn = Integer.valueOf(labelColumn) ;
	}

	public void setMsgColumn(String msgColumn) 
	{
		// TODO Auto-generated method stub
		 this.msgColumn = Integer.valueOf(msgColumn) ;
	}
	
	public void setPositiveCat(String category) 
	{
		// TODO Auto-generated method stub
		category = category.trim();
		 this.positiveCat = new ArrayList<String>(Arrays.asList(category.split(","))) ;
	}
	
	
	public void setNegativeCat(String category) 
	{
		// TODO Auto-generated method stub
		
		 this.negativeCat = new ArrayList<String>(Arrays.asList(category.split(","))) ;
	}
	
	

	
}
