package com.research.course.debate.preproc;

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
import java.util.HashMap;
import java.util.List;

import com.research.course.debate.preproc.PrepareDebateDataFilePerTopic.DebateInstance;



public class PrepareDebateDataFilePerTopic {

	/**
	 * @param args
	 */
	
	@SuppressWarnings("rawtypes")
	public
	class DebateInstance implements Comparable
	{
		public int getDebateId() {
			return debateId;
		}
		public void setDebateId(int debateId) {
			this.debateId = debateId;
		}
		public String getSpeakerId() {
			return speakerId;
		}
		public void setSpeakerId(String speakerId) {
			this.speakerId = speakerId;
		}
		public String getPoliticsRef() {
			return politicsRef;
		}
		public void setPoliticsRef(String politicsRef) {
			this.politicsRef = politicsRef;
		}
		public String getMention() {
			return mention;
		}
		public void setMention(String mention) {
			this.mention = mention;
		}
		public String getVote() {
			return vote;
		}
		public void setVote(String vote) {
			this.vote = vote;
		}
		public int getHtmlPageId() {
			return htmlPageId;
		}
		public void setHtmlPageId(int htmlPageId) {
			this.htmlPageId = htmlPageId;
		}
		public int getSpeechSeqnId() {
			return speechSeqnId;
		}
		public void setSpeechSeqnId(int speechSeqnId) {
			this.speechSeqnId = speechSeqnId;
		}
		
		public String toString()
		{
			return 	debateId+"\t"+speakerId+"\t"+htmlPageId+"\t"+speechSeqnId+"\t"+politicsRef
					+"\t"+mention+"\t"+vote+"\t"+(speakerId+"-"+htmlPageId+"-"+ speechSeqnId)+"\t"+speech;
		}
		
		private int debateId;
		private String speakerId;
		private String politicsRef ;
		private String mention ;
		private String vote ;
		
		private int htmlPageId;
		private int speechSeqnId ;
		private String speech;
		
		@Override
		public int compareTo(Object arg0) 
		{
			// TODO Auto-generated method stub
			int htmlPageId1 = this.getHtmlPageId() ;
			int htmlPageId2 = ((DebateInstance) arg0).getHtmlPageId() ;
			
			
			if(htmlPageId1 > htmlPageId2)
			{
				return 1;
			}
			else if ( htmlPageId1 < htmlPageId2 )
			{
				return 0 ;
			}
			else 
			{
				//check the speech seqn
				int speechSeqnId1 = this.getSpeechSeqnId();
				int speechSeqnId2 = ((DebateInstance) arg0).getSpeechSeqnId() ;
				
				if ( speechSeqnId1 > speechSeqnId2 )
				{
					return 1 ;
				}
				else
				{
					return 0 ;
				}
			}
			
		}
		public void setText(String fullText) 
		{
			// TODO Auto-generated method stub
			this.speech = fullText ;
		}
		public String getText() {
			// TODO Auto-generated method stub
			return speech;
		}
		
		
		
		
	}

	private HashMap<Integer, List<DebateInstance>> debateDataMap;
	
	 
	
	public PrepareDebateDataFilePerTopic()
	{
		debateDataMap = new HashMap<Integer,List<DebateInstance> >();
	}
	
	@SuppressWarnings("unchecked")
	public void loadDataFromTestFolder( String path ) throws IOException
	{
		File file = new File ( path +"/topics_separate/" );
		String files[] = file.list();
		
		DebateInstance debateObj = null ;
		
		List<DebateInstance> debateInstancesList  = null ;
		
		for ( String f : files )
		{
			File fl = new File (path + "/topics_separate/" + f );
			
			if(fl.isDirectory())
			{
				continue ;
			}
			
			if (f.contains("Store"))
			{
				continue;
			}
			
			//016_400005_0103006_RMY.txt
			//016 = debate id
			//400005 = rep id (speaker identifier)
			//0103 = original HTML page id
			//006 = position of the speech in the debate - important for the order
			//R = republican
			//M = "bill is mentioned"
			//Y = "ground truth - vote
			
			//sort the files based on the order (timeline) as the debate progresses
			
			String features[] = f.split("_");
			String debateId = features[0];
			String speakerId = features[1];
			String htmlPageId = features[2].substring(0,4);
			String speechPosnId = features[2].substring(4,7);
			String politicalId = features[3].substring(0,1);
			String mentionId = features[3].substring(1,2);
			String voteId = features[3].substring(2,3);
			
		//	if(!debateId.contains("16"))
		//	{
		//		continue ;
		//	}
			
			String fullText = getTextFromFile(path+"/topics_separate/" + f);
			
			
			
			debateObj = new DebateInstance();
			debateObj.setDebateId(Integer.valueOf(debateId));
			debateObj.setSpeakerId(speakerId);
			debateObj.setHtmlPageId(Integer.valueOf(htmlPageId));
			debateObj.setSpeechSeqnId(Integer.valueOf(speechPosnId));
			debateObj.setPoliticsRef(politicalId);
			debateObj.setMention(mentionId);
			debateObj.setVote(voteId);
			debateObj.setText(fullText);
			
			debateInstancesList = debateDataMap.get(Integer.valueOf(debateId));
			if (null == debateInstancesList )
			{
				debateInstancesList = new ArrayList<DebateInstance>();
			}
			
			debateInstancesList.add(debateObj);
			debateDataMap.put(Integer.valueOf(debateId),debateInstancesList) ;
		}
		
	//	java.util.Collections.sort(debateInstancesList) ;
		Writer writer = null ;
		
		for ( Integer debate : debateDataMap.keySet() )
		{
			writer = new BufferedWriter( new OutputStreamWriter ( new FileOutputStream ( path + "/topics_together/"+debate +".txt"
					), "UTF8") );
			
			writer.write("DebateId"+"\t"+"SpeakerId"+"\t"+"HTMLPageId"+"\t"+
									   "SpeechSeqnId"+"\t"+"PoliticsRef" +"\t" + "Mention"+"\t"+
									   "Vote"+"\t"+"SpeechID"+"\t"+"Speech");
			writer.write("\n") ;
			
			debateInstancesList = debateDataMap.get(debate);
			java.util.Collections.sort(debateInstancesList);
			
			
			for ( DebateInstance di : debateInstancesList)
			{
				writer.write(di.toString());
				writer.write("\n");
			}
			writer.close();
		}
		
	}
	
	private String getTextFromFile(String file) throws IOException 
	{
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new InputStreamReader (
				new FileInputStream( file) , "UTF8") );
		
		StringBuffer buffer = new StringBuffer();
		
		while (true)
		{
			String line = reader.readLine();
			if (line == null)
			{
				break;
			}

			buffer.append(line.trim());
			buffer.append(" ");
		}
		
		reader.close();
		
		return buffer.toString();
	}
	
	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		
		String path = "data/convote_v1.1/data_stage_one/development_set/";
		
		PrepareDebateDataFilePerTopic debateDataObj = new PrepareDebateDataFilePerTopic();
		debateDataObj.loadDataFromTestFolder(path);
	}



}
