package com.deft.sarcasm.postprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

public class GenerateOPPPDataToRelease 
{

	/**
	 * @param args
	 */
	
	private static final int MAX_COUNT = 5 ;
	private int smamId =  1 ;
	
	private int amamId =  1 ;
	
	
	public GenerateOPPPDataToRelease()
	{
		
	}
	
	public void readOPPPFiles ( String path ) throws IOException
	{
		//we will release only those data which are both in SM-AM and AM-AM corpus
		//and also they should be less than 5 (max five)
		String op_firstFile = "sarcasm.sample.op.first" ;
		String op_secondFile = "sarcasm.sample.op.second" ;
		String pp_firstFile = "sarcasm.sample.pp.first" ;
		String pp_secondFile = "sarcasm.sample.pp.right" ;
		
		
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path   + op_firstFile), "UTF8"));
	
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path   + op_secondFile), "UTF8"));
	
		BufferedReader reader3 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path  + pp_firstFile), "UTF8"));
	
		BufferedReader reader4 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path  + pp_secondFile), "UTF8"));
	
		//we need to maintain list - because every key is repeatable
		List<String> opLeftList = new ArrayList<String>();
		List<String> opRightList = new ArrayList<String>();
		List<String> ppLeftList = new ArrayList<String>();
		List<String> ppRightList = new ArrayList<String>();
		
		//open both the op files 
		
		while(true)
		{
			String line1 = reader1.readLine() ;
			String line2 = reader2.readLine() ;
			
			if ( null == line1 || null == line2)
			{
				break;
			}
			
			
			
			opLeftList.add(line1);
			opRightList.add(line2);
			
		}
		
		reader1.close();
		reader2.close();

// open both the pp files
		
		while(true)
		{
			String line1 = reader3.readLine() ;
			String line2 = reader4.readLine() ;
			
			if ( null == line1 || null == line2)
			{
				break;
			}
			
			ppLeftList.add(line1);
			ppRightList.add(line2);
			
		}
		
		reader3.close();
		reader4.close();
		
//now write the files		
		String opPath = "./data/twitter_corpus/release/" ;
		BufferedWriter writer1 = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (opPath + "/" + "sm-am-bitext.xml" ), "UTF8")) ;
	
		BufferedWriter writer2 = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (opPath + "/" + "am-am-bitext.xml" ), "UTF8")) ;
	
		
		preprocessSMAMFile(writer1);
		preprocessAMAMFile(writer2);
		
		//final writing
		for ( int i = 0 ; i < opLeftList.size() ; i++)
		{
			String opLeft = opLeftList.get(i);
			String opRight = opRightList.get(i);
			
			
			writeSMAMFile(writer1,opLeft,opRight);
	
		}
			
		for ( int i = 0 ; i < ppLeftList.size() ; i++)
		{
		
			String ppLeft = ppLeftList.get(i);
			String ppRight = ppRightList.get(i);
	
			
	//		opLeft = "on a bad note ,  it&apos;s always awful to hear what&apos;s going wrong ,  and not what&apos;s going right .";
			
	//		String orig = StringEscapeUtils.unescapeXml(opLeft) ;
			
	//		String xml = StringEscapeUtils.escapeXml(orig) ;
				
			writeAMAMFile(writer2,ppLeft,ppRight);
			
		}
		writer1.write("</SM-AM_Bitext_Corpus>");
		writer1.newLine();
		
		writer2.write("</AM-AM_Bitext_Corpus>");
		writer2.newLine();
	
		
		writer1.close();
		writer2.close();
		
		
	}
	
	public void readOPPPFiles2 ( String path ) throws IOException
	{
		//we will release only those data which are both in SM-AM and AM-AM corpus
		//and also they should be less than 5 (max five)
		String op_firstFile = "sarcasm.sample.op.first" ;
		String op_secondFile = "sarcasm.sample.op.second" ;
		String pp_firstFile = "sarcasm.sample.pp.first" ;
		String pp_secondFile = "sarcasm.sample.pp.right" ;
		
		
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path   + op_firstFile), "UTF8"));
	
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path   + op_secondFile), "UTF8"));
	
		BufferedReader reader3 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path  + pp_firstFile), "UTF8"));
	
		BufferedReader reader4 = new BufferedReader(new InputStreamReader(
				new FileInputStream(path  + pp_secondFile), "UTF8"));
	
		//we need to maintain list - because every key is repeatable
		List<String> opLeftList = new ArrayList<String>();
		List<String> opRightList = new ArrayList<String>();
		List<String> ppLeftList = new ArrayList<String>();
		List<String> ppRightList = new ArrayList<String>();
		
		Map<String,Integer> opLeftRightCountMap = new HashMap<String,Integer>() ;
		Map<String,List<String>> ppLeftRightMap = new HashMap<String,List<String>>();
		
		List<String> values = null ;
		
		//open both the op files 
		
		while(true)
		{
			String line1 = reader1.readLine() ;
			String line2 = reader2.readLine() ;
			
			if ( null == line1 || null == line2)
			{
				break;
			}
			
			Integer old = opLeftRightCountMap.get(line1);
			if ( null == old )
			{
				old = 0 ;
			}
			if ( old == MAX_COUNT)
			{
				continue  ;
			}
			
			opLeftRightCountMap.put(line1, old+1) ;
			
			opLeftList.add(line1);
			opRightList.add(line2);
			
		}
		
		reader1.close();
		reader2.close();

// open both the pp files
		
		while(true)
		{
			String line1 = reader3.readLine() ;
			String line2 = reader4.readLine() ;
			
			if ( null == line1 || null == line2)
			{
				break;
			}
			
			ppLeftList.add(line1);
			ppRightList.add(line2);
			
			values = ppLeftRightMap.get(line1);
			if ( null == values )
			{
				values = new ArrayList<String>();
			}
			values.add(line2);
			ppLeftRightMap.put(line1, values);
		}
		
		reader3.close();
		reader4.close();
		
//now write the files		
		String opPath = "./data/twitter_corpus/release/" ;
		BufferedWriter writer1 = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (opPath + "/" + "sm-am-bitext.xml" ), "UTF8")) ;
	
		BufferedWriter writer2 = new BufferedWriter ( new OutputStreamWriter 
				( new FileOutputStream (opPath + "/" + "am-am-bitext.xml" ), "UTF8")) ;
	
		
		preprocessSMAMFile(writer1);
		preprocessAMAMFile(writer2);
		
		//final writing
		for ( int i = 0 ; i < opLeftList.size() ; i++)
		{
			String opLeft = opLeftList.get(i);
			String opRight = opRightList.get(i);
			
	//		opLeft = "on a bad note ,  it&apos;s always awful to hear what&apos;s going wrong ,  and not what&apos;s going right .";
			
	//		String orig = StringEscapeUtils.unescapeXml(opLeft) ;
			
	//		String xml = StringEscapeUtils.escapeXml(orig) ;
			
		
			if(ppLeftList.contains(opRight))
			{
				
				values = ppLeftRightMap.get(opRight) ;
				if ( null == values )
				{
					System.out.println("wrong mapping");
					continue ;
				}
				
				writeSMAMFile(writer1,opLeft,opRight);
				
		//	writeAMAMFile(writer2,opRight,values);
				
				
			}
			else
			{
				//it might be an error!
		//		System.out.println("error - check the line: "+i + " " + opRight) ;
			}
		}
		
		writer1.write("</SM-AM_Bitext_Corpus>");
		writer1.newLine();
		
		writer2.write("</AM-AM_Bitext_Corpus>");
		writer2.newLine();
		
		
		writer1.close();
		writer2.close();
		
		
	}
	
	
	private void writeAMAMFile(BufferedWriter writer1, String ppLeft,
			String ppRight ) throws IOException 
	{
		// TODO Auto-generated method stub
		
			writer1.write("<pair id="+"\""+amamId+"\""+"><AM>"+ppLeft+"</AM>");
			writer1.newLine();
			
			writer1.write("<AM>"+ppRight+"</AM>");
			writer1.newLine();
		//<AM>I hate shopping on black friday.</AM>
		//</pair>
			writer1.write("</pair>");
			writer1.newLine();
			amamId++ ;
		
	}

	private void writeSMAMFile(BufferedWriter writer1, String opLeft,
			String opRight) throws IOException 
	{
		// TODO Auto-generated method stub
		//<pair id="1"><SM>I love shopping on black friday.</SM>
		writer1.write("<pair id="+"\""+smamId+"\""+"><SM>"+opLeft+"</SM>");
		writer1.newLine();
		
		writer1.write("<AM>"+opRight+"</AM>");
		writer1.newLine();
		//<AM>I hate shopping on black friday.</AM>
		//</pair>
		writer1.write("</pair>");
		writer1.newLine();
		
		smamId++ ;
	}

	private void preprocessAMAMFile(BufferedWriter writer2) throws IOException 
	{
		// TODO Auto-generated method stub
		writer2.write("<?xml version=" + "\""+"1.0"+"\""+ " encoding=" +"\""+"UTF-8" +"\""+"?>");
		writer2.newLine();
		
		writer2.write("<AM-AM_Bitext_Corpus>") ;
		writer2.newLine() ;
	}

	private void preprocessSMAMFile(BufferedWriter writer1) throws IOException 
	{
		// TODO Auto-generated method stub
		writer1.write("<?xml version=" + "\""+"1.0"+"\""+ " encoding=" +"\""+"UTF-8" +"\""+"?>");
		writer1.newLine();
		
		writer1.write("<SM-AM_Bitext_Corpus>") ;
		writer1.newLine() ;
		
	}

	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		String path = "/Users/dg513/work/sw/moses/mosesdecoder/working/corpus/" ;
		
		GenerateOPPPDataToRelease opppObj = new GenerateOPPPDataToRelease() ;
		
		opppObj.readOPPPFiles(path) ;
	}
	
	
}
