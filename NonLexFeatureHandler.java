package com.deft.sarcasm.features;

import java.io.IOException;

import com.deft.sarcasm.train.SarcasmResourceLoader;

public class NonLexFeatureHandler 
{

	private SarcasmResourceLoader sarcasmRLObject;
	private String unigramPath;
	private String nonLexFeatureFile;
	private EXPERIMENT_MODE experMode;

	public NonLexFeatureHandler(EXPERIMENT_MODE experMode, SarcasmResourceLoader sarcasmRLObject)
	{
		this.sarcasmRLObject = sarcasmRLObject ;
		this.experMode = experMode ;
	}

	public void setNonLexFeatureFile(String nonLexFeatureFile,
			String unigramPath) 
	{
		// TODO Auto-generated method stub
		this.unigramPath = unigramPath ;
		this.nonLexFeatureFile = nonLexFeatureFile ;
	}

	public void loadNonLexFeatures() throws IOException 
	{
		// TODO Auto-generated method stub
//		sarcasmRLObject.loadGlobalNonLexFeatures(unigramPath, nonLexFeatureFile)	;

	}
}
