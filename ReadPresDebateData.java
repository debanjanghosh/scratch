package com.research.course.debate.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ReadPresDebateData {

	/**
	 * @param args
	 */
	public ReadPresDebateData()
	{
		
	}
	
	public void readDebateFile ( String path ) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( path + "obama_romney_debate_1.txt") , "UTF8") );
		
		Writer writer = new BufferedWriter( new OutputStreamWriter ( new FileOutputStream
				( path + "debate_output_1.txt"), "UTF8") );
		
		
		boolean romneyFlag = false ;
		boolean obamaFlag = false ;
		boolean modrtrFlag = false ;
		
		String argument = null ;
		String debateId = "1" ;
		String speakerId = null ;
		String htmlPageId = "1" ;
		int speechSeqn = 0 ;
		String polRef = null ;
		String mention = "Y" ;
		String vote = "Y" ;
		
		while (true)
		{
			String line = reader.readLine();
			if (line == null )
			{
				break;
			}
			
			if ( line.length() < 3)
			{
				continue ;
			}
			
			if(line.contains("to your own airplane and"))
			{
				System.out.println("here");
			}
			
			if (line.startsWith("ROMNEY") || line.startsWith("romney"))
			{
				speakerId = "500099" ;
				polRef = "ROMNEY" ;
				polRef = "R" ;
				if ( null == argument )
				{
					//start
					argument = line.substring(7,line.length());
				}
				else
				{
					//already in
					if (obamaFlag)
					{
						writeTheArgument(writer, debateId, speakerId, htmlPageId, speechSeqn,
								polRef, mention, vote, argument);
						speechSeqn++ ;
						argument = line.substring(7,line.length());
					}
					else if (modrtrFlag)
					{
						
					}
					else if (romneyFlag)
					{
						//the last one is also a romney one
						writeTheArgument(writer, debateId, speakerId, htmlPageId, speechSeqn, polRef,
								mention, vote, argument);
						speechSeqn++ ;
						argument = line.substring(7,line.length());
					}
					
				}
				
				romneyFlag = true ;
				modrtrFlag = false ;
				obamaFlag = false ;
				
			}
			
			else if (line.startsWith("OBAMA") || line.startsWith("obama"))
			{
				speakerId = "500100" ;
				polRef = "OBAMA" ;
				polRef = "D" ;
				if ( null == argument )
				{
					//start
					argument = line.substring(7,line.length());
				}
				else
				{
					//already in
					if (romneyFlag)
					{
						writeTheArgument(writer, debateId, speakerId, htmlPageId, speechSeqn, polRef,
								mention, vote, argument);
						speechSeqn++ ;
						argument = line.substring(7,line.length());
					}
					else if (modrtrFlag)
					{
						
					}
					else if (obamaFlag)
					{
						//the last one is also a obama one
						writeTheArgument(writer, debateId, speakerId, htmlPageId, speechSeqn, polRef, 
								mention, vote, argument);
						speechSeqn++ ;
						argument = line.substring(7,line.length());
					}
					
				}
				
				obamaFlag = true ;
				romneyFlag = false ;
				modrtrFlag = false ;
			}
			else if (line.startsWith("LEHRER"))
			{
				
				if (null != argument)
				{
					writeTheArgument(writer, debateId, speakerId, htmlPageId, speechSeqn, polRef, 
							mention, vote, argument);
					speechSeqn++ ;
				}
				modrtrFlag = true ;
				obamaFlag = false ;
				romneyFlag = false ;
				//
				
				argument = null ;
				continue ;
			}
			
			else
			{
				if (romneyFlag)
				{
					//add on romney
					argument = argument + " " + line ;
 				}
				if ( obamaFlag)
				{
					//add on obama
					argument = argument + " " + line ;
				}
				if (modrtrFlag)
				{
					continue ;
				}
			}
			
			
		}
		
		reader.close();
		writer.close();
		
	}
	
	private void writeTheArgument(Writer writer, String debateId, String speakerId, 
			String htmlPageId, 	int speechSeqn, String polRef, String mention, 
			String vote, String speech  ) throws IOException 
	{
		// TODO Auto-generated method stub
		speech = speech.trim();
		
		writer.write(debateId + "\t" +  speakerId + "\t" + 
				 htmlPageId + "\t" +  speechSeqn + "\t" +  polRef + "\t" +   mention + "\t" + 
				 vote + "\t" +   speech );
		writer.write("\n");
		
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String path = "./data/presi_debates_2012/" ;
		ReadPresDebateData readPresDebateObj  = new ReadPresDebateData();
		readPresDebateObj.readDebateFile(path) ;
	}

}
