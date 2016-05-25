package com.wsd.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
	
	private double filePresent ;

	private static final String configPath =  "./data/config" ;

	private String outputFile;

	private String modelFile;

	private String modelPath;
	
	private String outputPath ;
	
	private List<String> trainingWeeks ;
	
	private List<String> testingWeeks ;

	private List<String> targets;
	
	private class ScoreClass
	{
	
		public double getAvgP() {
			return avgP;
		}

		public void setAvgP(double avgP) {
			this.avgP = this.avgP  + avgP;
		}

		public double getAvgR() {
			return avgR;
		}

		public void setAvgR(double avgR) {
			this.avgR = this.avgR + avgR;
		}

		public double getAvgF1() {
			return avgF1;
		}

		public void setAvgF1(double avgF1) 
		{
			this.avgF1 = this.avgF1 + avgF1;
		}

		public double getMaxP() {
			return maxP;
		}

		public void setMaxP(double maxP) 
		{
			this.maxP = maxP;
			if (maxP > this.maxP)
			{
				this.maxP = maxP;
			}

		}

		public double getMaxR() {
			return maxR;
		}

		public void setMaxR(double maxR) 
		{
			if (maxR > this.maxR)
			{
				this.maxR = maxR;
			}
		}

		public double getMaxF1() {
			return maxF1;
		}

		public void setMaxF1(double maxF1) 
		{
						
			if (maxF1 > this.maxF1)
			{
				this.maxF1 = maxF1;
			}
		}

		public double getMinP() 
		{
			return minP;
		}

		public void setMinP(double minP) 
		{
			if (minP < this.minP)
			{
				this.minP = minP;
			}
		}

		public double getMinR() {
			return minR;
		}

		public void setMinR(double minR) 
		{
			if (minR < this.minR)
			{
				this.minR = minR;
			}
		}

		public double getMinF1() 
		{
			return minF1;
		}

		public void setMinF1(double minF1) 
		{
			if (minF1 < this.minF1)
			{
				this.minF1 = minF1;
			}
		}

		private double avgP = 0.0;
		private double avgR = 0.0;
		private double avgF1 = 0.0;
		
		private double maxP = Double.MIN_VALUE ;
		private double maxR = Double.MIN_VALUE;
		private double maxF1 = Double.MIN_VALUE;
		
		private double minP = Double.MAX_VALUE;
		private double minR = Double.MAX_VALUE;;
		private double minF1 = Double.MAX_VALUE;
		
		public void ScoreClass()
		{
			
		}
		
	}
	
	
	private Map<Double,ScoreClass> scoreForCategory ;
	
	public PRF1Measure()
	{
		scoreForCategory = new HashMap<Double,ScoreClass>() ;

	}
	
	
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
		
		ScoreClass scoreObj = null ;
		
		Set<Double> categories = eachCategoryMap.keySet();
		
		if ( categories.size() !=0)
		{
			filePresent++ ;
			
		}
		
		List<String> ops = new ArrayList<String>() ;
		
		DecimalFormat dec = new DecimalFormat("###.##") ;
		
		double totalTruePositives = 0.0 ; 
		double totalTrueNegatives = 0.0 ; 

		
		
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

			totalTruePositives = totalTruePositives + truePositive ;
		//	totalTrueNegatives = totalTrueNegatives + 
			
		//	String out = "category:" + " " + convert(category) + " " + "precision:" + " " + dec.format(precision()) + " " + "recall:"
		//			+ " " + dec.format(recall()) + " " +"FMeasure:" + " " + dec.format(FMeasure());
			double precision = precision() ;
			double recall = recall() ;
			double FMeasure = FMeasure() ;
			
			scoreObj = scoreForCategory.get(category);
			if ( null == scoreObj)
			{
				scoreObj = new ScoreClass() ;
			}
			scoreObj.setAvgF1(FMeasure);
			scoreObj.setAvgP(precision);
			scoreObj.setAvgR(recall);
			
			scoreObj.setMinF1(FMeasure);
			scoreObj.setMinR(recall);
			scoreObj.setMinP(precision);
			
			scoreObj.setMaxF1(FMeasure);
			scoreObj.setMaxR(recall);
			scoreObj.setMaxP(precision);
			
			scoreForCategory.put(category, scoreObj);
			
			
			String out = convert(category) + "\t" + dec.format(precision) + "\t" 
						+ dec.format(recall) + "\t" + dec.format(FMeasure);
		
			
			ops.add(out);
			selected = 0 ;
		}
		
		
		
		//macro level - 
		


		return ops ;
	}
	
	public List<String> calculateSeparatePRF1() 
	{
		// TODO Auto-generated method stub
		Set<Double> categories = eachCategoryMap.keySet();
		List<String> ops = new ArrayList<String>() ;
		
		DecimalFormat dec = new DecimalFormat("###.##") ;
		
		double totalTruePositives = 0.0 ; 
		double totalTrueNegatives = 0.0 ; 

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

			totalTruePositives = totalTruePositives + truePositive ;
		//	totalTrueNegatives = totalTrueNegatives + 
			
		//	String out = "category:" + " " + convert(category) + " " + "precision:" + " " + dec.format(precision()) + " " + "recall:"
		//			+ " " + dec.format(recall()) + " " +"FMeasure:" + " " + dec.format(FMeasure());
			double precision = precision() ;
			double recall = recall() ;
			double FMeasure = FMeasure() ;
			
		//	String out = convert(category) + "\t" + dec.format(precision) + "\t" 
		//				+ dec.format(recall) + "\t" + dec.format(FMeasure);
			
			String out = (category) + "\t" + dec.format(precision) + "\t" 
					+ dec.format(recall) + "\t" + dec.format(FMeasure);
	
			
			
			ops.add(out);
			selected = 0 ;
		}
			return ops ;
	}
	
	private void printCumulative()
	{
		DecimalFormat dec = new DecimalFormat("###.##") ;
		
		for (Double category : scoreForCategory.keySet()) 
		{
			ScoreClass scoreObj = scoreForCategory.get(category);
			
			double avgP = scoreObj.getAvgP()/(double)filePresent;
			double avgR = scoreObj.getAvgR()/(double)filePresent;
			double avgF1 =  scoreObj.getAvgF1()/(double)filePresent ;
			
	//		System.out.println(category + "\t" + avgP + "\t" + avgR + "\t" + avgF1) ;
	//		System.out.println(category + "\t" +scoreObj.getMinP() + "\t" + scoreObj.getMinR() + "\t" + scoreObj.getMinF1()) ;
	//		System.out.println(category + "\t" +scoreObj.getMaxP() + "\t" + scoreObj.getMaxR() + "\t" + scoreObj.getMaxF1()) ;

			System.out.println(category + "\t" + dec.format(avgP) + "(+" + dec.format(scoreObj.getMaxP() -  avgP) + "/-" + dec.format(avgP - scoreObj.getMinP() )+ ")"
				 + "\t" +	 dec.format(avgR) + "(+" + dec.format( scoreObj.getMaxR()  - avgR)  + "/-" + dec.format(avgR - scoreObj.getMinR() )+ ")"
					 + "\t" + dec.format(avgF1) + "(+" + dec.format (scoreObj.getMaxF1() - avgF1)  + "/-" + dec.format(avgF1 - scoreObj.getMinF1() )+ ")" ) ;
		}
		
	}

	private String convert(Double category) 
	{
		// TODO Auto-generated method stub
		if ( category.doubleValue() == 2.0 || category.doubleValue() == 0.0)
		{
			return "NON_SARCASM" ; 
		}
		else if ( category.doubleValue() == 1.0)
		{
			return "SARCASM" ;
		}
		
		return null;
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

	public void updateScores(List<RefTargetPair> refPredList, Double category) 
	{

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
	
	public void loadMultipleOutputFiles ( ) throws IOException
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
			String trainFile = fileName.substring(5,fileName.indexOf("test") );
			String test = fileName.substring(fileName.indexOf("test")+4,fileName.indexOf(".")) ;
			String testFile = "test_week_" + test + ".dat.binary.svm.TESTING.txt" ;
			
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
			
			
			System.out.println("TEST FILE: " + testFile) ;
			System.out.println("OUTPUT FILE FROM SVM: " + fileName) ;
			

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
			
			
			System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;
		
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
	
	public void loadTargets () throws IOException
	{
		
		String path = "./data/config" ;
		String file = "topnames.txt" ;
		targets = Files.readAllLines(Paths.get(path + "/" + file),
				StandardCharsets.UTF_8);

		System.out.println("all targets are loaded ") ;
	}
	
	
	public void loadFiles ( ) throws IOException
	{
		// get the property value and print it out
		featurePath = "./data/output/prf1_outputs" ;
		modelPath = featurePath ;
		
		File file = new File(modelPath) ;
		File[] files = file.listFiles() ;
		
		System.out.println("Target" + "\t" + "Category" + "\t" + "Precision" + "\t"
		+ "Recall" + "\t" + "F1");
		
		for (String target : targets )
		{
			
	//		if(!target.equalsIgnoreCase("always"))
	//		{
	//			continue ;
	//		}
			
			if (!(target.equals("good") || target.equals("brilliant") ||
					target.equals("love") || target.equals("cute")))
			{
		//		continue ;
			}
			
			 featureFile = "tweet."  + target + ".weiwei.svm.feature" ;
			 outputFile =  "tweet."   +target + ".weiwei.svm.op" ;
		
	//		 featureFile = "tweet."  + target + ".svm.feature" ;
	//		 outputFile =  "tweet."   +target + ".svm.op" ;
			 
			 featureFile = "tweet."  + target + ".gensim.svm.feature" ;
			 outputFile =  "tweet."   +target + ".gensim.svm.op" ;

			 
			 File f = new File (featurePath + "/" + featureFile) ;
				if (! f.exists())
				{
					continue ;
				}
				
		
			 
			 String testFile = featurePath + "/" + featureFile ;
			 String opFile = modelPath + "/" + outputFile ;
		
	//		 System.out.println("TEST FILE: " + testFile) ;
	//		 System.out.println("OUTPUT FILE FROM SVM: " + opFile) ;
	//		 System.out.println(target+":");
	
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
				
				String features1[] = line1.split("\t") ;
				String snum1 = features1[0] ;
				double reference = Double.valueOf(features1[1]).doubleValue() ;
				
				String features2[] = line2.split("\t") ;
				String snum2 = features2[0] ;
				double predict = Double.valueOf(features2[1]).doubleValue() ;
				
				if ( !snum1.equalsIgnoreCase(snum2))
				{
					System.out.println("snum are not matching - check ") ;
					continue ;
				}
				
				//for p/r/f1
				refTargetPairList = eachCategoryMap.get(reference);
				if ( null == refTargetPairList )
				{
					refTargetPairList = new ArrayList<RefTargetPair>();
				}
		
				pair = new RefTargetPair(reference,predict);
				refTargetPairList.add(pair);
				eachCategoryMap.put((double)reference, refTargetPairList);
		
				lineNum++ ;
			}
			 
	//		 System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;
			 reader2.close();
			 reader1.close();
			 
			 //now calculate
			List<String> ops = calculatePRF1();
			for ( String op : ops )
			{
				if ( op.contains("category: 0.0"))
				{
					continue ;
				}

				System.out.println(target + "\t" +lineNum + "\t" + op);
				eachCategoryMap.clear();
			} 
		}

		printCumulative();
	}
	
	public void loadClassificationFilesOnAllTargets ( ) throws IOException
	{
		// get the property value and print it out
		featurePath = "./data/model/" ;
		modelPath = featurePath ;
		
		featureFile = "test.temp" ;
		outputFile =  "test.pred" ;
		
		 String testFile = featurePath + "/" + featureFile ;
		 String opFile = modelPath + "/" + outputFile ;
		
	//		 System.out.println("TEST FILE: " + testFile) ;
	//		 System.out.println("OUTPUT FILE FROM SVM: " + opFile) ;
	//		 System.out.println(target+":");
	
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
				
			String features1[] = line1.split("\t") ;
	//		String snum1 = features1[0] ;
			double reference = Double.valueOf(features1[0]).doubleValue() ;
				
			String features2[] = line2.split("\t") ;
	//		String snum2 = features2[0] ;
			double predict = Double.valueOf(features2[0]).doubleValue() ;
				
		/*	
				if ( !snum1.equalsIgnoreCase(snum2))
				{
					System.out.println("snum are not matching - check ") ;
					continue ;
				}
			*/	
				//for p/r/f1
			refTargetPairList = eachCategoryMap.get(reference);
			if ( null == refTargetPairList )
			{
				refTargetPairList = new ArrayList<RefTargetPair>();
			}
		
			pair = new RefTargetPair(reference,predict);
			refTargetPairList.add(pair);
			eachCategoryMap.put((double)reference, refTargetPairList);
		
			lineNum++ ;
		}
			 
	//		 System.out.println("NUMBER OF DATA INSTANCES: " + lineNum) ;
		 reader2.close();
		 reader1.close();
			 
			 //now calculate
		List<String> ops = calculatePRF1();
		for ( String op : ops )
		{
			if ( op.contains("category: 0.0"))
			{
				continue ;
			}

			System.out.println(target + "\t" +lineNum + "\t" + op);
			eachCategoryMap.clear();
		} 
		

		printCumulative();
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
	

		input.close() ;
	}
	
	public static void main (String[] args ) throws IOException
	{
		
		String configFile = args[1] ;
		String type = args[2];
		
		PRF1Measure prf1CalcObj = new PRF1Measure();
		
		prf1CalcObj.activate(configFile);
		if ( type.equalsIgnoreCase("multiple"))
		{
	//		prf1CalcObj.loadMultipleOutputFiles() ;
			prf1CalcObj.loadClassificationFilesOnAllTargets();

		}
		if ( type.equalsIgnoreCase("single"))
		{
			prf1CalcObj.loadTargets() ;
			prf1CalcObj.loadFiles();	
		/*	
			List<String> ops = prf1CalcObj.calculatePRF1();
			for ( String op : ops )
			{
				if ( op.contains("category: 0.0"))
				{
					continue ;
				}

				System.out.println(op);
			}
		*/	
		}
	
	}

	public void setPRParameters(
			Map<Double, List<RefTargetPair>> eachCategoryMap) 
	{
		// TODO Auto-generated method stub
	 this.eachCategoryMap = eachCategoryMap ;
	}
	

}
