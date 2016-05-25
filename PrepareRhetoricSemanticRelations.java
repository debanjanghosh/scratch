package com.research.course.debate.preproc;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.research.course.debate.rsrelations.CauseParser;
import com.research.course.debate.rsrelations.ContrastParser;
import com.research.course.debate.rsrelations.Pair;
import com.research.course.debate.rsrelations.RhetoricParser;
import com.research.course.debate.util.CreatingLanguageModel;

import jtextpro.JTextProcessor;

import opennlp.tools.sentdetect.*;
import opennlp.tools.util.InvalidFormatException;

public class PrepareRhetoricSemanticRelations {

	/**
	 * @param args
	 */
	
	public enum Rhetorics
	{
		CONTRAST,
		CAUSE
	}
	
	private JTextProcessor jtProcObj ;
	private final String JTPModelPath = "./data/models/";
	
	 static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
	// static final SentenceModel SENTENCE_MODEL  = new MedlineSentenceModel();


	 private RhetoricParser rhetoricParser ;

	private CreatingLanguageModel lmObj ;
	private SentenceDetectorME _sentenceDetector;
	private Rhetorics rhObj ;
	 
	 
	public PrepareRhetoricSemanticRelations(Rhetorics rhetoric)
	{
//		jtProcObj = new JTextProcessor();
//	    jtProcObj.setModelPath("./data/models/");
//	    jtProcObj.initJTextProObject();
		
		if(rhetoric.equals(Rhetorics.CAUSE))
		{
			rhetoricParser = new CauseParser();
		}
		if (rhetoric.equals(Rhetorics.CONTRAST) )
		{
			rhetoricParser = new ContrastParser();
		}
		
		rhObj = rhetoric ;
	   
	    lmObj = new CreatingLanguageModel();
	}
	

	
	public void loadEachDebateTopics( String input, String output, String fold ) throws IOException
	{
		File file = new File ( input + "/"+ fold + "/" + "/topics_together/" );
		String files[] = file.list() ;
		
		System.out.println("number of files in " + fold + " is "+ files.length ) ;
		
		
		BufferedReader reader = null ;
		BufferedWriter writer = new BufferedWriter (new FileWriter ( output+"d-r-condition-cause-" +
		fold +".nostop.0208.txt"));
		
		Map<String,Integer> eachDebateMap = null ;
		int index = 0 ;
		
		for ( String f : files )
		{
			File fl = new File (input + "/" + fold + "/"+ "/topics_together/" + f );
		
			System.out.println(" file processing is " + fold + " : " + f );
			
			
			if (fl.isDirectory())
			{
				continue ;
			}
			if ( f.contains("Store") || f.contains("zip"))
			{
				continue ;
			}
			
			String debate = f.substring(0,f.indexOf(".")) ;
/*			
			if ( debate.equals("16"))
			{
				System.out.println("here1");
				
			}
			else
			{
				continue ;
			}
	*/		
			eachDebateMap = new HashMap<String,Integer>();
			
			
			reader = new BufferedReader(new InputStreamReader (
					new FileInputStream( input + "/" + fold + "/" +  "/topics_together/" + f) , "UTF8") );
			
			
			//header
			String line = reader.readLine();
			
			while (true)
			{
				line = reader.readLine();
				if (line == null)
				{
					break;
				}
				
				String features[] = line.split("\t");
				
				String politics = features[4];
				
				String speech = features[7];
				
		//		speech = speech.replace(" .",".");
		//		speech = speech.replace(" ?", "?");
		//		speech = speech.replace(" !", "!");
				
		//		String[] speeches = _sentenceDetector.sentDetect(speech) ;
				
				
		//		


				boolean conPresent  = rhetoricParser.checkRhetoricAvail(speech,rhObj) ;
					
				

				List<Pair> contPairs = null ;
				if ( conPresent )
				{
				//	contPairs = conParseObj.ListContrastExamples(speech) ;
					contPairs = rhetoricParser.ListExamples(speech,rhObj) ;
					for ( Pair p : contPairs )
					{
					//	System.out.println(debate +"\t" + politics +"\t" + p.getLeft() +"\t<RHETORIC>\t" + p.getRight()) ;
						//build up the bigram data points 
						String dataPoint = lmObj.createBigramPairs((String)p.getLeft(),(String) p.getRight(),fold,politics);
						
			//			System.out.println(dataPoint);
			//			writer.write(dataPoint);
			//			writer.newLine();
						
						if(politics.contains("R"))
						{
							writer.write(0+" "+ dataPoint);
							writer.newLine();
				//			System.out.println(0+" "+dataPoint);
						}
						if (politics.contains("D"))
						{
							writer.write(1+" "+ dataPoint);
							writer.newLine();
				//			System.out.println(1+" "+dataPoint);
						}
						
						index++;
						if ( ( index % 1000 ) == 0 )
						{
							System.out.println("condition # "+ index + " created. ");	
						}
					}
					
					
				}				
				
				
				
			}
			
			reader.close();
			
		}
		writer.close();
		
//		lmObj.createWekaBigramData(output,fold);
		
	}
	
	public void createWekaBigramData()
	{
	//	lmObj.createWekaBigramData();
	}
	
/*	
	private List<String> getLPSentences(String speech) 
	{
		// TODO Auto-generated method stub
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		List<String> lpSentences = new ArrayList<String>();
		
		
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(speech.toCharArray(),0,speech.length());
		tokenizer.tokenize(tokenList,whiteList);

		System.out.println(tokenList.size() + " TOKENS");
		System.out.println(whiteList.size() + " WHITESPACES");

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
	//	int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,whites);

		System.out.println(sentenceBoundaries.length 
				   + " SENTENCE END TOKEN OFFSETS");
			
		if (sentenceBoundaries.length < 1) 
		{
		    System.out.println("No sentence boundaries found.");
		    
		}
		int sentStartTok = 0;
		int sentEndTok = 0;
		for (int i = 0; i < sentenceBoundaries.length; ++i)
		{
		    sentEndTok = sentenceBoundaries[i];
		    System.out.println("SENTENCE "+(i+1)+": ");
		    for (int j=sentStartTok; j<=sentEndTok; j++)
		    {
		    	System.out.print(tokens[j]+whites[j+1]);
		    	lpSentences.add(tokens[j]+whites[j+1]);
		    }
		    
		    System.out.println();
		    sentStartTok = sentEndTok+1;
		}
		return lpSentences;
	}
*/
	private void loadStopWords( String config ) throws IOException
	{
		lmObj.loadStopWords(config);
	}

	private void loadOpenNLPSentDet() 
	{
		// TODO Auto-generated method stub
	//	conParseObj.loadOpenNLPSentDet();
		rhetoricParser.loadOpenNLPSentDet();
		
	}

	
	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		String input = "data/convote_v1.1/data_stage_one/";
		String output = "data/output/output_format/" ;
		String config = "data/config/";
		
	//	String folds[] = {"training_temp_set", "development_temp_set", "test_temp_set"} ;
		String folds[] = {"training_set", "development_set", "test_set", "presdebate_set"} ;
		
		PrepareRhetoricSemanticRelations prepRhetoricStatementRelObj = 
				new PrepareRhetoricSemanticRelations(Rhetorics.CONTRAST) ;
	
		prepRhetoricStatementRelObj.loadOpenNLPSentDet();
		prepRhetoricStatementRelObj.loadStopWords(config);
		
		for ( String fold : folds )
		{
			prepRhetoricStatementRelObj.loadEachDebateTopics(input,output,fold);
		}
		
	}

	
}
