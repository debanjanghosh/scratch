package com.deft.sarcasm.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CreateTrainTestFolders 
{

	/**
	 * @param args
	 */
	
	private static Integer FOLD_NUM = 5 ;
	
	public CreateTrainTestFolders()
	{
		
	}
	
	public void readOriginalFileAndCreateFolds ( String path ) throws IOException
	{
//		String foldPath = path + "/output/svm/5fold/";
		String foldPath = path + "/5fold/";

		
	//	String file = "rawcorpus_s_p.txt.binary.svm.TRAINING.txt" ;
	//	String file = "tweet.SARCNOSARC.ONLY.CONTEXT.TRAIN.prev_current.binary.svm.TRAINING.txt" ;
	//	String file = "tweet.SARCNOSARC.ONLY.CONTEXT.TRAIN.filtered.english.05122016.current.binary.svm.TRAINING.txt" ;
		String file = "tweet.SARCNOSARC.CONTEXT.txt.current.svm.TRAINING.num.txt" ;
	//	String file = "tweet.SARCNOSARC.CONTEXT.txt" ;


		//get the relation name
			
		String relation = "SARCASM";
		//load the relation file
		List<String> docs = read(path + file);
		//randomize the list
		java.util.Collections.shuffle(docs);
			
		int size = docs.size();
		int foldSize = size/FOLD_NUM ;
		foldSize +=1 ; //why? to take the very last few lines
		int index = 0;
		Writer writer = null ;
		for (int i = 1; i<=FOLD_NUM; i++)
		{
				
			String folder = getName(i);
			writer = new BufferedWriter ( new OutputStreamWriter (new FileOutputStream
					(foldPath + folder +"/" + file + "." + folder),"UTF8"));
			
			for ( int j = index ; j < foldSize * i ;j++)
			{
				String doc = null ;
				try
				{
					doc = docs.get(j);
				}
				catch(IndexOutOfBoundsException e)
				{
						//so we have crossed the limit of docs
						//do nothing? 
					writer.close();
					break;
						
				}
					
				{
					writer.write(doc);
					writer.write("\n");
				}
			}
			index = foldSize*i;
			writer.close();
				
			System.out.println("finish writing "+ folder +"/" + file + "." + folder) ;
		}
			
			
		
	}
	
	private String getRelName(String file) 
	{
		// TODO Auto-generated method stub
		if(file.contains("Kill"))
			return "Kill" ;
		if(file.contains("Live_In"))
			return "Live_In" ;
		if(file.contains("OrgBased_In"))
			return "OrgBased_In" ;
		if(file.contains("Located_In"))
			return "Located_In" ;
		if(file.contains("Work_For"))
			return "Work_For" ;
		
		return null;
	}

	private String getName(int i)
	{
		// TODO Auto-generated method stub
		switch (i)
		{
			case 1:
				return "one";
			case 2:
				return "two" ;
			case 3:
				return "three" ;
			case 4:
				return "four" ;
			case 5:
				return "five" ;
			default:
				break;
				
		}
		return "none";
	}

	private List<String> read(String file) throws IOException 
	{
		// TODO Auto-generated method stub
		List<String> docs = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader( new InputStreamReader
				(
				new FileInputStream (file), "UTF8") );

		while (true)
		{
			String line = reader.readLine();
			if ( line == null )
			{
				break;
			}
			docs.add(line.trim());
		}
		reader.close();
		return docs;
	}

	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
	//	String input = "./data/oldpython/rawcorpus_s_p/svm/";
	//	String input = "./data/twitter_corpus/output/svm/";
		String input = "/Users/dg513/work/eclipse-workspace/sarcasm-workspace/sarcasm_dialogue/Corpus/output/svm/" ;
	//	String input = "/Users/dg513/work/eclipse-workspace/sarcasm-workspace/sarcasm_dialogue/Corpus/" ;

		
		CreateTrainTestFolders createTrainTestObj = new CreateTrainTestFolders();
		createTrainTestObj.readOriginalFileAndCreateFolds(input);
	}

}
