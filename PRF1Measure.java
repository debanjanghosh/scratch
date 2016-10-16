package com.deft.sarcasm.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PRF1Measure {

	@SuppressWarnings("rawtypes")
	// for each category - maintain a hash of ref/target pairs
	private Map<Double, List<RefTargetPair>> eachCategoryMap;

	/**
	 * |selected| = true positives + false positives <br>
	 * the count of selected (or retrieved) items
	 */
	private long selected;

	/**
	 * |target| = true positives + false negatives <br>
	 * the count of target (or correct) items
	 */
	private long target;

	private long truePositive;

	private String featurePath;

	private String featureFile;

	private static final String configPath =  "./data/config" ;

	private String outputFile;

	private String modelFile;

	private String modelPath;
	
	private String outputPath ;
	
	private List<String> trainingWeeks ;
	
	private List<String> testingWeeks ;

	public void setPrecRecallObject(
			HashMap<Double, List<RefTargetPair>> eachCategoryMap) {
		// TODO Auto-generated method stub
		this.eachCategoryMap = eachCategoryMap;
		
		System.out.println("length of the map is1 " + this.eachCategoryMap.keySet().size()) ;
		System.out.println("length of the map is2 " + this.eachCategoryMap.keySet().size()) ;
		
		
	}

	public List<String> calculatePRF1() 
	{
		// TODO Auto-generated method stub
		Set<Double> categories = eachCategoryMap.keySet();
		
		List<String> ops = new ArrayList<String>() ;
		
		DecimalFormat dec = new DecimalFormat("###.##") ;
		
		
		for (Double category : categories) 
		{
			@SuppressWarnings("rawtypes")
			List<RefTargetPair> refPairList = eachCategoryMap.get(category);
			updateScores(refPairList, category);
			for (Double c : eachCategoryMap.keySet()) 
			{
				refPairList = eachCategoryMap.get(c);
				updateSelected(refPairList, category);
			}

			String out = "category:" + "\t" + category + "\t" + "precision:" + "\t" + dec.format(precision()) + "\t" + "recall:"
					+ "\t" + dec.format(recall()) + "\t" +"FMeasure:" + "\t" + dec.format(FMeasure());
		
			ops.add(out);
			selected = 0 ;
		}
		
		//macro level - 

		return ops ;
	}
	
	public List<String> calculatePRF1(String targetName) 
	{
		// TODO Auto-generated method stub
		Set<Double> categories = eachCategoryMap.keySet();
		
		List<String> ops = new ArrayList<String>() ;
		
		DecimalFormat dec = new DecimalFormat("###.##") ;
		
		
		for (Double category : categories) 
		{
			@SuppressWarnings("rawtypes")
			List<RefTargetPair> refPairList = eachCategoryMap.get(category);
			updateScores(refPairList, category);
			for (Double c : eachCategoryMap.keySet()) 
			{
				refPairList = eachCategoryMap.get(c);
				updateSelected(refPairList, category);
			}

		//	String out = "category:" + " " + category + " " + "precision:" + " " + dec.format(precision()) + " " + "recall:"
		//			+ " " + dec.format(recall()) + " " +"FMeasure:" + " " + dec.format(FMeasure());
		
			String out = targetName + "\t" + convert(String.valueOf(category)) + "\t"  + dec.format(precision()) + 
					"\t" +  dec.format(recall()) + "\t" + dec.format(FMeasure());
		
			
			ops.add(out);
			selected = 0 ;
		}
		
		//macro level - 

		return ops ;
	}

	private String convert(String category)
	{
		if ( category.equalsIgnoreCase("0.0"))
		{
			return "NON_SARCASM" ;
		}
		if ( category.equalsIgnoreCase("1.0"))
		{
			return "SARCASM" ;
		}
		return null ;
	}
	
	public void updateSelected(List<RefTargetPair> refPredList, double category) {
		for (RefTargetPair pair : refPredList) 
		{
			Double s =  (Double) pair.getRight() ;
			if (s.equals(category)) 
			{
				selected++;
			}
		}
	}

	public void updateScores(List<RefTargetPair> refPredList, Double category) {

		truePositive = countTruePositives(refPredList);
		target = refPredList.size();
	}

	public double precision() 
	{
		double precision =  selected > 0 ? (double) truePositive / (double) selected : 0;
		precision *= 100d ;
		return precision ;
	}

	public double recall() 
	{
		double recall =  target > 0 ? (double) truePositive / (double) target : 0;
		recall *= 100d ;
		return recall ;
	}

	public double FMeasure() 
	{
		
		if (precision() + recall() > 0) 
		{
			double f1 = 2 * (precision() * recall()) / (precision() + recall());
			return f1;
		}
		else
		{
			// cannot divide by zero, return error code
			return -1;
		}
	}

	static int countTruePositives(List<RefTargetPair> refPredList) 
	{
		int truePositives = 0;

		// Note: Maybe a map should be used to improve performance
		for (int referenceIndex = 0; referenceIndex < refPredList.size(); referenceIndex++) 
		{
			RefTargetPair refObject = refPredList.get(referenceIndex);

			Double referenceName = (Double) refObject.getLeft();
			Double predName = (Double) refObject.getRight() ;
		//		System.out.println(" ref " + referenceName + " " + "pred " + predName) ;
			if (referenceName.equals(predName)) 
			{
					truePositives++;
			}
			
			
		//	
		}
		
		
		
		return truePositives;
	}
	
	public void loadWeeklyMultipleOutputFiles ( ) throws IOException
	{
		//we are loading multiple output files and features files here
		System.out.println("OUTPUT PATH: " + outputPath);
		File file = new File(outputPath) ;
		File[] files = file.listFiles();
		
		BufferedReader reader1 = null ; 
		BufferedReader reader2 = null ;
				
		for ( File f : files )
		{
			if (f.isDirectory())
			{
				continue ;
			}
			
			if(!f.getName().contains("op"))
			{
				continue ;
			}
			
			String fileName = f.getName() ;
//old =			String trainFile = fileName.substring(5,fileName.indexOf("test") );
			String trainFile = fileName.split("\\.")[0];

			String test = fileName.split("\\.")[1] ;
			String testFile = test + ".all.filtered.02012016.selected.binary.svm.TESTING.txt" ;
			
		//	String opFile = test + ".op" ;
			//check the training testing weeks
			if(!trainingWeeks.contains(trainFile))
			{
				continue ;
			}
			
			if(!testingWeeks.contains(test))
			{
				continue ;
			}
			
			
	//		System.out.println("TEST FILE: " + testFile) ;
	//		System.out.println("OUTPUT FILE FROM SVM: " + fileName) ;
			

			reader1 = new BufferedReader ( new FileReader ( featurePath + "/" + testFile)) ;
			reader2 = new BufferedReader ( new FileReader ( outputPath + "/" + fileName)) ;
			
			List<RefTargetPair> refTargetPairList = null ;
			eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
			RefTargetPair pair = null ;


			int lineNum = 0 ;
			
			while (true )
			{
				String line1 = reader1.readLine();
				String line2 = reader2.readLine();
				
				if ( line1 == null || line2 == null )
				{
					break ;
				}
				
				double reference = Double.valueOf(line1.split("\\s")[0].trim()).doubleValue() ;
				double target = Double.valueOf(line2.trim()).doubleValue();
				
				//for p/r/f1
				refTargetPairList = eachCategoryMap.get(reference);
				if ( null == refTargetPairList )
				{
					refTargetPairList = new ArrayList<RefTargetPair>();
				}
			
				pair = new RefTargetPair(reference,target);
				refTargetPairList.add(pair);
				eachCategoryMap.put((double)reference, refTargetPairList);
		
				lineNum++ ;
			}
			
			
		//	System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;
		
			reader2.close();
			reader1.close();

			//for each combination of train/test - we need to find the P/R/F1
			
			List<String> ops = calculatePRF1();
			for ( String op : ops )
			{
				if ( op.contains("category: 0.0"))
				{
					continue ;
				}

				System.out.println("TRAIN WEEK:" + "\t" + trainFile + "\t" + "TEST WEEK:" + 
				"\t" + test + "\t" + op);
			}
			eachCategoryMap.clear();
		}
		
		
	}
	
	
	public void loadWSDFiles (String targetName ) throws IOException
	{
		// get the property value and print it out
		
		{
		//	modelPath = "./lib/" ;
			modelPath = "./data/twitter_corpus/models/samelm2/" ;
			featureFile = "tweet."+targetName+".target.TEST.binary.svm.TESTING.txt";
			
			outputFile = targetName + ".op" ;
			
			String testFile = outputPath + "/" + featureFile ;
			String opFile = modelPath + "/" + outputFile ;
			
//			System.out.println("TEST FILE: " + testFile) ;
//			System.out.println("OUTPUT FILE FROM SVM: " + opFile) ;

			BufferedReader reader1 = new BufferedReader ( new FileReader ( testFile)) ;
			BufferedReader reader2 = new BufferedReader ( new FileReader ( opFile)) ;
		
			List<RefTargetPair> refTargetPairList = null ;
			eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
			RefTargetPair pair = null ;
	
			int lineNum = 0 ;
				
			while (true )
			{
				String line1 = reader1.readLine();
				String line2 = reader2.readLine();
				
				if ( line1 == null || line2 == null )
				{
					break ;
				}
				
				double reference = Double.valueOf(line1.split("\\s")[0].trim()).doubleValue() ;
				double target = Double.valueOf(line2.trim()).doubleValue();
				
				//for p/r/f1
				refTargetPairList = eachCategoryMap.get(reference);
				if ( null == refTargetPairList )
				{
					refTargetPairList = new ArrayList<RefTargetPair>();
				}
			
				pair = new RefTargetPair(reference,target);
				refTargetPairList.add(pair);
				eachCategoryMap.put((double)reference, refTargetPairList);
		
				lineNum++ ;
				
			}
			
	//		System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;

			reader2.close();
			reader1.close();
		}
	}
	
	public void loadFiles (  String fold ) throws IOException
	{
		// get the property value and print it out
		
		String testFile = featurePath + "/" + fold + "/" + featureFile + fold ;
		String opFile = modelPath + "/" + fold + outputFile ;
		
		System.out.println("TEST FILE: " + testFile) ;
		System.out.println("OUTPUT FILE FROM SVM: " + opFile) ;
		

		BufferedReader reader1 = new BufferedReader ( new FileReader ( testFile)) ;
		BufferedReader reader2 = new BufferedReader ( new FileReader ( opFile)) ;
		
		
		List<RefTargetPair> refTargetPairList = null ;
		eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
		RefTargetPair pair = null ;


		int lineNum = 0 ;
		
		while (true )
		{
			String line1 = reader1.readLine();
			String line2 = reader2.readLine();
			
			if ( line1 == null || line2 == null )
			{
				break ;
			}
			
			double reference = Double.valueOf(line1.split("\\s")[0].trim()).doubleValue() ;
			double target = Double.valueOf(line2.trim()).doubleValue();
			
			//for p/r/f1
			refTargetPairList = eachCategoryMap.get(reference);
			if ( null == refTargetPairList )
			{
				refTargetPairList = new ArrayList<RefTargetPair>();
			}
		
			pair = new RefTargetPair(reference,target);
			refTargetPairList.add(pair);
			eachCategoryMap.put((double)reference, refTargetPairList);
	
			lineNum++ ;
			
		}
		
		System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;
		
		reader2.close();
		reader1.close();

	}
	
	public void loadFiles ( ) throws IOException
	{
		// get the property value and print it out
		
		String testFile = featurePath + "/" + featureFile ;
		String opFile = modelPath + "/" + outputFile ;
		
		System.out.println("TEST FILE: " + testFile) ;
		System.out.println("OUTPUT FILE FROM SVM: " + opFile) ;
		

		BufferedReader reader1 = new BufferedReader ( new FileReader ( testFile)) ;
		BufferedReader reader2 = new BufferedReader ( new FileReader ( opFile)) ;
		
		
		List<RefTargetPair> refTargetPairList = null ;
		eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
		RefTargetPair pair = null ;


		int lineNum = 0 ;
		
		while (true )
		{
			String line1 = reader1.readLine();
			String line2 = reader2.readLine();
			
			if ( line1 == null || line2 == null )
			{
				break ;
			}
			
			double reference = Double.valueOf(line1.split("\\s")[0].trim()).doubleValue() ;
			double target = Double.valueOf(line2.trim()).doubleValue();
			
			//for p/r/f1
			refTargetPairList = eachCategoryMap.get(reference);
			if ( null == refTargetPairList )
			{
				refTargetPairList = new ArrayList<RefTargetPair>();
			}
		
			pair = new RefTargetPair(reference,target);
			refTargetPairList.add(pair);
			eachCategoryMap.put((double)reference, refTargetPairList);
	
			lineNum++ ;
			
		}
		
		System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;
		
		reader2.close();
		reader1.close();

	}

	

	public void activate ( String configFile) throws IOException
	{
		Properties prop = new Properties();
		InputStream input = null;
	 		
		input = new FileInputStream(configPath + "/" +configFile);
	 
		// load a properties file
		prop.load(input);
	 
		// get the property value and print it out
		featurePath = prop.getProperty("FeaturePath");
		featureFile = prop.getProperty("featureFile");
		modelPath = prop.getProperty("modelPath");
		outputPath = prop.getProperty("outputPath");
		outputFile = prop.getProperty("outputFile");
		modelFile = prop.getProperty("modelFile") ;
		String training[] = null ;
		try
		{
			training = prop.getProperty("trainingWeeks").split(",") ;
		}
		catch (NullPointerException e )
		{
			//probably this config file do not have training/testing for weekly experiment
			//so return from here 
			input.close();
			return ;
		}
		trainingWeeks = new ArrayList<String>(Arrays.asList(training));
		
		String testing[] = prop.getProperty("testingWeeks").split(",") ;
		testingWeeks = new ArrayList<String>(Arrays.asList(testing));

		input.close() ;
	}
	
	public static void main (String[] args ) throws IOException
	{
		
		String configFile = args[1] ;
		String type = args[2];
	//	type = "wsd" ;
	//	type = "single" ;
		PRF1Measure prf1CalcObj = new PRF1Measure();
		prf1CalcObj.activate(configFile);
		if ( type.equalsIgnoreCase("multiple"))
		{
			prf1CalcObj.loadWeeklyMultipleOutputFiles() ;
		}
		else if ( type.equalsIgnoreCase("single"))
		{
			
			prf1CalcObj.loadFiles();	
			List<String> ops = prf1CalcObj.calculatePRF1();
			for ( String op : ops )
			{
				if ( op.contains("category: 0.0"))
				{
				//	continue ;
				}

				System.out.println(op);
			}
		}
		else if ( type.equalsIgnoreCase("context"))
		{
			String folds[] = {"one", "two", "three", "four", "five" } ;
			
			for ( String fold : folds )
			{
				prf1CalcObj.loadFiles(fold);	
				List<String> ops = prf1CalcObj.calculatePRF1();
				for ( String op : ops )
				{
					if ( op.contains("category: 0.0"))
					{
				//		continue ;
					}
	
					System.out.println(op);
				}
			}
		}

		else if ( type.equalsIgnoreCase("wsd"))
		{
			List<String> targets = prf1CalcObj.loadTargets();
		//	String[] targets = {"yeah"};
			for ( String target : targets )
			{
		//		if ( target.equalsIgnoreCase("mature") || target.equalsIgnoreCase("shocked"))
		//		{
		//			continue ;
			//	}
				prf1CalcObj.loadWSDFiles(target);	
				List<String> ops = prf1CalcObj.calculatePRF1(target);
				for ( String op : ops )
				{
				//	if ( op.contains("category: 0.0") || op.contains("NON_SARCASM"))
				//	{
				//		continue ;
				//	}
					if( op.contains("NON_SARCASM"))
					{
						continue ;
					}
					System.out.println(op);
				}
			}
		}
	}

	private List<String> loadTargets() throws IOException 
	{
		// TODO Auto-generated method stub
		String path = "./data/config/" ;
		String file = "topnames.txt" ;
		return Files.readAllLines(Paths.get(path + "/" + file),
				StandardCharsets.UTF_8);
	}
	

}
