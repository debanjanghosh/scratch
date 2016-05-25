package com.wsd.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.python.util.PythonInterpreter;
import org.python.core.*;

//from gensim.models import word2vec


public class JythonTest 
{

	/**
	 * @param args
	 */
	
	public JythonTest()
	{
		
	}
	
	public void checkSimilarityOfTwoWords()
	{
		PythonInterpreter interp = new PythonInterpreter();
		interp.execfile("./src/com/wsd/util/GensimInterface.py");
		
		PyFunction func =
				(PyFunction)interp.get("printHelloWorld",PyFunction.class);
		
		func.__call__();
		
	/*	
		PyFunction func =
		(PyFunction)interp.get("loadGensimModel",PyFunction.class);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh.mm.ss");
		System.out.println("======[" + sdf.format(new Date()) +
		"]===========");
		func.__call__(new PyString("cbow"));
		System.out.println("======[" + sdf.format(new Date()) +
		"]===========");
		
		String tweeter_1 = "this is a test tweet" ;
		String tweeter_2 = "also a test tweet" ;
		
		String[] tweeter_1_words = tweeter_1.split("\\s++") ;
		String[] tweeter_2_words = tweeter_2.split("\\s++") ;
		
		func =
				(PyFunction)interp.get("computeSimViaWord2Vec",PyFunction.class);
		
		PyFloat similarity = (PyFloat) func.__call__(new PyString(tweeter_1),new PyString(tweeter_2));

		System.out.println("similarity is " + similarity.asDouble()) ;

		*/
	}
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		JythonTest testObject = new JythonTest() ;
		testObject.checkSimilarityOfTwoWords();
	}

}
