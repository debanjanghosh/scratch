package com.deft.sarcasm.features;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.deft.sarcasm.train.SarcasmResourceLoader;


public class LexicalPragFeatureLoader extends FeatureLoader
{
	
	private SarcasmResourceLoader sarcasmRLObject ;

	private static String LEX_PRAG_PATH  ;

	
	//this function will be deprecated 
	public LexicalPragFeatureLoader() throws IOException
	{
		loadDictionaries();
	}
	
	
	public LexicalPragFeatureLoader(SarcasmResourceLoader sarcasmRLObject) throws IOException
	{
		this.sarcasmRLObject = sarcasmRLObject ;
	}
	
	public void setLexPath ( String resourcePath) throws IOException
	{
		LEX_PRAG_PATH = resourcePath ;
		loadDictionaries();

	}


	protected void loadDictionaries() throws IOException
	{
		sarcasmRLObject.loadLIWCDictionaries(LEX_PRAG_PATH);
	}

	@Override
	public Map<String, Double> loadNonLexFeatures(String[] tokens) 
	{
		// TODO Auto-generated method stub
		Map<String, Double> featureMap = new HashMap<String, Double>();
		
		for (String token : tokens) 
		{
			token = StringEscapeUtils.escapeXml(token);
			
			List<String> featColumnNames = getFeatureColumn(token);
			if (!featColumnNames.isEmpty()) // that is the feature is
											// present in the token
			{
				for (String featColumnName : featColumnNames) 
				{
					
				//	if(featColumnName.equals("66.csv"))
				//	{
				//		System.out.println("here") ;
					//}
					Double old = featureMap.get(featColumnName);
					if (null == old) 
					{
						old = 0.0;
					}
					
					featureMap.put(featColumnName, old + 1.0);
				}
			}
		}
		
		return featureMap ;
	}

	private List<String> getFeatureColumn(String token) 
	{
		// TODO Auto-generated method stub
		return sarcasmRLObject.getListOfColumns(token);

	}


	@Override
	public Map<Integer, Double> loadFeatures(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
