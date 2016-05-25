package com.research.course.debate.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.research.course.debate.rsrelations.CauseParser;
import com.research.course.debate.rsrelations.ContrastParser;
import com.research.course.debate.rsrelations.Pair;

public class StatisticsOnSemanticRhetorics {

	private ContrastParser conParseObj;
	private CauseParser causeParseObj ;


	/**
	 * @param args
	 */
	
	public StatisticsOnSemanticRhetorics()
	{
		  conParseObj = new ContrastParser();
		  causeParseObj = new CauseParser() ;
	}
	
	public void loadEachDebateTopics( String input, String output, String fold ) throws IOException
	{
		File file = new File ( input + "/"+ fold + "/" + "/topics_together/" );
		String files[] = file.list() ;
		
	//	System.out.println("number of files in " + fold + " is "+ files.length ) ;
		
		
		BufferedReader reader = null ;
		BufferedWriter writer = new BufferedWriter (new FileWriter ( output+"d-r-condition-" +
		fold +".nostop.1121.txt"));
		
		Map<String,Integer> eachDebateMap = null ;
		
		System.out.println("debate"+"\t"+"Start Vote"+"\t"+"Start Politics"+"\t"+"Rep Cont."+
		"\t"+"Dem Cont."+"\t" + "total Cont." + "Start Win") ;
		
		int startWinCount = 0 ;
		int startLossCount = 0 ;
		
		int fileCount = 0 ;
		
		for ( String f : files )
		{
			File fl = new File (input + "/" + fold + "/"+ "/topics_together/" + f );
		
			
			if (fl.isDirectory())
			{
				continue ;
			}
			if ( f.contains("Store") || f.contains("zip"))
			{
				continue ;
			}
			
		//	System.out.println(" file processing is " + fold + " : " + f );
			
			
			String debate = f.substring(0,f.indexOf(".")) ;
		/*	
			if ( debate.contains("16"))
			{
				System.out.println("here1");
				
			}
			else
			{
				continue ;
			}
		*/	
			fileCount++ ;
			
			
			eachDebateMap = new HashMap<String,Integer>();
			
			
			reader = new BufferedReader(new InputStreamReader (
					new FileInputStream( input + "/" + fold + "/" +  "/topics_together/" + f) , "UTF8") );
			
			
			//header
			String line = reader.readLine();
			int index = 0 ;
			String startPolitics = null ;
			String startVote = null ;
			
			int rCount = 0 ;
			int dCount = 0 ;
			int totalCount = 0 ; 
			
			int voteMatrix[][] = new int[2][2] ;
			voteMatrix[0] = new int[2];
			voteMatrix[1] = new int[2];
			
			
			while (true)
			{
				line = reader.readLine();
				if (line == null)
				{
					break;
				}
				
				String features[] = line.split("\t");
				
				
				String politics = features[4];
				String vote = features[6];
				String speech = features[7];
				
				if ( index == 0 )
				{
					startPolitics = politics ;
					startVote = vote ;
				}
				
		//		int conCount = conParseObj.checkContrastCount(speech) ;
				int conCount = causeParseObj.checkCauseCount(speech) ;
				
				
				totalCount += conCount ;
				
				if ( conCount > 0 )
				{
					
			//		System.out.println(debate+"\t"+vote+"\t"+politics +"\t" + conCount);
					
				}				
				
				if ( politics.equalsIgnoreCase("R"))
				{
					rCount += conCount ;
					
					if (vote.equalsIgnoreCase("y"))
					{
						voteMatrix[1][0]++;
					}
					
					if (vote.equalsIgnoreCase("n"))
					{
						voteMatrix[1][1]++;
					}
				}
				if ( politics.equalsIgnoreCase("D"))
				{
					dCount += conCount ;
					
					if (vote.equalsIgnoreCase("y"))
					{
						voteMatrix[0][0]++;
					}
					
					if (vote.equalsIgnoreCase("n"))
					{
						voteMatrix[0][1]++;
					}
				}
				
				index++ ;
				
			}
			
			reader.close();
			boolean startWin = false ;
			if ( startPolitics.equalsIgnoreCase("R"))
			{
				if ( rCount >= dCount )
				{
					startWin = true ;
					startWinCount++;
				}
				else
				{
					startLossCount++ ;
				}
			}
			else if ( startPolitics.equalsIgnoreCase("D"))
			{
				if ( dCount >= rCount )
				{
					startWin = true ;
					startWinCount++ ;
				}
				else
				{
					startLossCount++ ;
				}
			}
			
			
			System.out.println(debate+"\t"+startVote+"\t"+startPolitics +"\t" + rCount
					+ "\t" + dCount +"\t" +  totalCount +"\t"+startWin);
	//		System.out.println(" ");
			
	//		System.out.println("\t"+"Y"+"\t"+"N");
	//		System.out.println("D"+"\t"+voteMatrix[0][0]+"\t" + voteMatrix[0][1]);
	///		System.out.println("R"+"\t"+voteMatrix[1][0]+"\t" + voteMatrix[1][1]);
			
	//		System.out.println(" ");
			
		}
		writer.close();
		
		System.out.println("number of files in " + fold + " is "+ fileCount ) ;
		System.out.println(startWinCount +"\t"+startLossCount) ;
	}
	
	
	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		String input = "data/convote_v1.1/data_stage_one/";
		String output = "data/output/output_format/" ;
		String config = "data/config/";
		
	//	String folds[] = {"training_temp_set", "development_temp_set", "test_temp_set"} ;
		String folds[] = {"training_set", "development_set", "test_set"} ;
		
		
		StatisticsOnSemanticRhetorics statOnRhetoricObj = new StatisticsOnSemanticRhetorics();
		
		for ( String fold : folds )
		{
		
			statOnRhetoricObj.loadEachDebateTopics(input, output, fold) ;
		}
	}

}
