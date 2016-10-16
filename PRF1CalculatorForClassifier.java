package com.deft.sarcasm.postprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



public class PRF1CalculatorForClassifier
{

    /**
     * @param args
     */
    /**
     * @param args
     */
    
    public class EachDataPoint implements Comparable
    {
        private String id ;
        private double score ;
        
        public EachDataPoint(String id, double score)
        {
            this.id  = id ;
            this.score = score ;
        }

        @Override
        public int compareTo( Object o )
        {
            // TODO Auto-generated method stub
            EachDataPoint data = (EachDataPoint) o;

            // if(data.opVal != this.opVal)
            return Double.compare( this.score, data.score );
  
        }
    }
    
    ArrayList<String> testLabels;
    ArrayList<String> opLabels ;
    private ArrayList<Double> categories;
    private ArrayList<EachDataPoint> dataPointList;
    
    private static final int LIBSVM = 1 ;
    private static final int SVMLIGHT = 2 ;
    
    private Map<Double,List<Float>> precs ;
    private Map<Double,List<Float>> recs ;
	private static int DIRECTION;
    
	public class Scoring
	{
		public double getPrecision() {
			return precision;
		}

		public void setPrecision(double precision) {
			this.precision = precision;
		}

		public double getRecall() {
			return recall;
		}

		public void setRecall(double recall) {
			this.recall = recall;
		}

		public double getF1() 
		{
			f1 = (2*precision*recall)/(precision + recall) ;
			return f1;
		}

		public void setF1(double f1) {
			this.f1 = f1;
		}

		private double precision ;
		private double recall ;
		private double f1 ;
		
		
		
		public Scoring()
		{
			
		}
		
		
		
	}

	double microCorrect ;
	double microPredict ;
	double microInput ;
	
	
    public PRF1CalculatorForClassifier(int catNum)
    {
        categories = new ArrayList<Double> ();
        
        for ( int i = 0 ; i < catNum ;i++)
        {
        	categories.add( Double.valueOf( i ) );
        }
        precs = new HashMap<Double,List<Float>>();
        recs = new HashMap<Double,List<Float>>();
        
        DIRECTION = 2 ;
        
        
        
    }

    public void init()
    {
    	 testLabels = new ArrayList<String>();
         opLabels = new ArrayList<String>();
         dataPointList = new  ArrayList<EachDataPoint> ();
         
  
    }
    
    public void free()
    {
    	testLabels.clear();
    	opLabels.clear();
    	
    	
    }
    
    private ArrayList<String> getFileList( String baseDirPath, String op )
    {
        File srcDir = new File( baseDirPath );
        File[] allFiles = srcDir.listFiles();
        ArrayList<String> opFiles = new ArrayList<String>();
        for ( File file : allFiles )
        {
            if ( !(file.getName().endsWith( op )) )
                continue;

            if ( file.isDirectory() )
                continue;

            opFiles.add( file.getName() );

        }
        return opFiles;

    }
    
    
    public void loadOutputFileForNoZero( String dirPath, String opFolder, String dataFolder,
            String fileNm, String extn, String folds[]) throws IOException
    {
         dataPointList = new  ArrayList<EachDataPoint> ();
         ArrayList<String> opFiles = getFileList( dirPath + opFolder, extn );
         BufferedReader readerOPFile = null;
         BufferedReader readerTestFile = null;

        final String test = ".test";
         final String slash = "/";
         for ( String opFile : opFiles )
         {
             String outFold = null;
             for ( String fold : folds )
                 if ( opFile.indexOf( fold, 0 ) > -1 )
                        outFold = fold;

             if ( outFold == null ) 
                 continue;

            String SVMOpFile = dirPath + opFolder + opFile;
            String SVMFeatFile =
                dirPath + dataFolder + outFold + slash +  fileNm+outFold;// + test + outFold;

            System.out.println( SVMOpFile );
            System.out.println( SVMFeatFile );

            readerOPFile = new BufferedReader( new FileReader( SVMOpFile ) );
            readerTestFile = new BufferedReader( new FileReader( SVMFeatFile ) );
            
            while ( true )
            {
               
                
                String testLine = readerTestFile.readLine();
                if (  testLine == null)
                    break;
                
                String features[] = testLine.split( "\\s" );
                String label = features[0];
                
       
               // if (label.equalsIgnoreCase("0"))
                //	continue ;
                
                String opLine = readerOPFile.readLine();
                
                if ( opLine == null )
                    break;
                
                String id = features[features.length-1];
                
                testLabels.add(label);
                opLabels.add(opLine);
                
                int cat = Integer.valueOf( label ).intValue();
                double score = Double.valueOf( opLine ).doubleValue();
                if(cat == -1)
                {
                    EachDataPoint dataObj = new EachDataPoint( id, score );
                    dataPointList.add(dataObj);
                }
                
            }           
            readerOPFile.close();
            readerTestFile.close();
         }



    }
    
    public void loadSingleOutputFile () throws IOException
    {
    	  categories = new ArrayList<Double>();
    	  
    	  String SVMOpFile = "alltrainrandom.op";
    	//  String SVMOpFile = "alltrainsent.op";
    	  
    	  
    	  //s-p-n new (weiwei data)
    //	  String SVMFeatFile = "s2pn.02132014.filtered.shuffled.test.binary.svm.somehash.s2pn.txt.2000" ;
    	  String SVMFeatFile = "s2pn.spanish.02192014.svm.TEST.txt.10000" ;
    	  //sarthak's file - 
    	  SVMFeatFile = "TestData.tweet.txt.binary.svm.TESTING.txt" ;
    	  
    	  SVMFeatFile = "tweet.imbalanced.testing.sarcnonsarc.06062014.binary.svm.TESTING.txt" ;
    	  
    	  SVMFeatFile = "tweeter.imbalanced.spanish.testing.sarcnonsarc.07022014.txt.sgml.binary.svm.TESTING.txt" ;
      	
    	  SVMFeatFile = "tweet.like.target.TEST.binary.svm.TESTING.txt" ;
    	  
    	  SVMFeatFile = "tweet.ALLTARGETS.SARCNOSARC.TEST.binary.svm.TESTING.txt" ;
    //	  SVMFeatFile = "tweet.ALLTARGETS.SARCSENTIMENT.TEST.binary.svm.TESTING.txt" ;
    	  
    	  
    	  //s-p-n testing file
       //   String SVMFeatFile = "raw_human_judges_testingaggr_s_p_n.txt.01072014.binary.fv.svm.spn.txt";// + test + outFold;
          
          //s-ns testing file
   //       SVMFeatFile = "rawcorpus_with_human_judges_s_ns_emoticons_testing.txt.01072014.binary.fv.svm.sns.txt" ;

          System.out.println( SVMOpFile );
          System.out.println( SVMFeatFile );

          BufferedReader readerTestFile = new BufferedReader( new FileReader( "./data/twitter_corpus/output/svm/wsd/allData/" + SVMFeatFile ) );
       //   BufferedReader readerTestFile = new BufferedReader( new FileReader( "./data/Spanish_Data/output/svm/" + SVMFeatFile ) );
          
          BufferedReader readerOPFile = new BufferedReader( new FileReader( "./lib/" +  SVMOpFile) );
            
          while ( true )
          {
        	  String opLine = readerOPFile.readLine();
              String testLine = readerTestFile.readLine();
              if ( opLine == null || testLine == null)
            	  break;
                
                String features[] = testLine.split( "\\s" );
                String label = features[0]; //why not features[0] and features[1]? because features[1] is 
                //now the label and 0 is the line number
                
                if(!categories.contains(Double.valueOf(label)))
                {
                	categories.add(Double.valueOf(label)) ;
                }
                
                String id = features[features.length-1];
                
                testLabels.add(label);
                opLabels.add(opLine);
                
                int cat = Integer.valueOf( label ).intValue();
                double score = Double.valueOf( opLine ).doubleValue();
                if(cat == -1)
                {
                    EachDataPoint dataObj = new EachDataPoint( id, score );
                    dataPointList.add(dataObj);
                }
                
            }           
            readerOPFile.close();
            readerTestFile.close();
    }
    
    public void loadOutputFile( String dirPath, String opFolder, String dataFolder,
            String fileNm, String extn, String folds[]) throws IOException
    {
         dataPointList = new  ArrayList<EachDataPoint> ();
         ArrayList<String> opFiles = getFileList( dirPath + opFolder, extn );
         BufferedReader readerOPFile = null;
         BufferedReader readerTestFile = null;

        final String test = ".test";
         final String slash = "/";
         
         categories = new ArrayList<Double>();
         
         for ( String opFile : opFiles )
         {
             String outFold = null;
             for ( String fold : folds )
                 if ( opFile.indexOf( fold, 0 ) > -1 )
                        outFold = fold;

             if ( outFold == null ) 
                 continue;

             //data/CCGData/both/
             
            String SVMOpFile = dirPath + opFolder + opFile;
            String SVMFeatFile =
                dirPath + dataFolder + outFold + slash +  fileNm+outFold;// + test + outFold;

            System.out.println( SVMOpFile );
            System.out.println( SVMFeatFile );

            readerOPFile = new BufferedReader( new FileReader( SVMOpFile ) );
            readerTestFile = new BufferedReader( new FileReader( SVMFeatFile ) );
            
            while ( true )
            {
                String opLine = readerOPFile.readLine();
                String testLine = readerTestFile.readLine();
                if ( opLine == null || testLine == null)
                    break;
                
                String features[] = testLine.split( "\\s" );
                String label = features[0]; //why not features[0] and features[1]? because features[1] is 
                //now the label and 0 is the line number
                
                if(!categories.contains(Double.valueOf(label)))
                {
                	categories.add(Double.valueOf(label)) ;
                }
                
                String id = features[features.length-1];
                
                testLabels.add(label);
                opLabels.add(opLine);
                
                int cat = Integer.valueOf( label ).intValue();
                double score = Double.valueOf( opLine ).doubleValue();
                if(cat == -1)
                {
                    EachDataPoint dataObj = new EachDataPoint( id, score );
                    dataPointList.add(dataObj);
                }
                
            }           
            readerOPFile.close();
            readerTestFile.close();
         }



    }
    
    public void printTopDataFromAnyCategory() throws IOException
    {
        java.util.Collections.sort( dataPointList ) ;
        
        BufferedWriter writer = new BufferedWriter ( new FileWriter("data/urduSRL/"+"urdu.topneg.20k.linear.bl.txt"));
        writer.write( "id" +"\t"+"score" );
        writer.newLine();
        
        int i = 0;
        		
        for ( EachDataPoint datapoint : dataPointList)
        {
            writer.write( datapoint.id + "\t" + datapoint.score );
            writer.newLine();
            
            i++;
            if(i==20000)
                break;
        }
        
        writer.close();
        
    }
    
    
    public void computePerformance(int type)
    {
       
        //so no zeroes j == 1
    	List<Float> precisions = null ;
    	List<Float> recalls = null ;
    	
        for ( int j = 0 ; j < categories.size(); j++)
        {
        	double category = categories.get(j);
            double correct = 0 ;
            double allPredict = 0;
            double allInput = 0;
            
            for ( int i = 0 ; i <testLabels.size();i++ )
            {
                double ip = getNorm( Double.valueOf( testLabels.get(i)).doubleValue() , type);
                double op = getNorm(Double.valueOf( opLabels.get(i)).doubleValue() , type ) ; 
                
                if ( DIRECTION == 1 && ip == 2.0)
                {
                	ip = 1.0;
                }
                
                
                if(ip ==category && ip == op )
                {
                    correct++ ;
                }
                
                if(ip == category)
                {
                    allInput++ ;
                }
                
                if(op == category )
                {
                    allPredict++ ;
                }
            }
            double recall = (correct/allInput)*100 ;
            double precision = (correct/allPredict)*100 ;
            
            precisions = precs.get(category);
            if (null == precisions )
            {
            	precisions = new ArrayList<Float>();
            }
            precisions.add((float)precision);
            precs.put(category,precisions);
            
            recalls = recs.get(category);
            if ( null == recalls )
            {
            	recalls = new ArrayList<Float>();
            }
            recalls.add((float)recall);
            recs.put(category,recalls);
           
            /* micro cal */
            microCorrect += correct ;
            microPredict += allPredict ;
            microInput += allInput ;
           
            double f1 = (2*precision*recall)/(precision+recall) ;
            System.out.println("all input for category "+category+" = " +allInput);
            System.out.println("all correct for category "+category+" = " + correct);
     
            System.out.println("all predicted for category "+category+" = " + allPredict);
            
      //      System.out.println("precision for category "+ category+ " = " + precision);
       //     System.out.println("recall for category "+category+" = "+ recall);
        //    System.out.println("f1 for category "+ category+ " = " + f1);
        }
    
        
        
       
    }
    private double getNorm( double op, int algo )
    {
        // TODO Auto-generated method stub
        if(algo == LIBSVM)
            return op ;
        else if (algo == SVMLIGHT)
        
        if ( op >= 0 )
            op = 1.0;
        if ( op < 0 )
            op = -1.0;
       
        return op ;
    }
    
    private void printPerformance(int svm_type) 
	{
		// TODO Auto-generated method stub
    	DecimalFormat dec = new DecimalFormat("###.##");
    	
    	double macroPrecision = 0.0 ;
    	double macroRecall = 0.0 ;
    	double macroF1 = 0.0 ;
    	
    	double microPrecision = 0.0 ;
    	double microRecall = 0.0 ;
    	double microF1 = 0.0 ;
		
		for ( Double cat : precs.keySet())
		{
			List<Float> p = precs.get(cat); 
			List<Float> r = recs.get(cat); 
			
			float totalPrec = (float) 0.0 ;
			float totalRecs = (float) 0.0 ;
			float totalF1 = (float) 0.0 ;
			
			for ( int i = 0 ; i < p.size();i++ )
			{
				float prec = p.get(i);
				float rec = r.get(i);
				totalPrec += prec ;
				totalRecs +=  rec ;
			
				float eachF1 = (2 * prec * rec)/(rec  + prec) ;
				System.out.println("cat " + cat + " p = "+prec + " r = "+rec + " f1 = "+eachF1);
			}
			
			totalPrec = totalPrec / (float)p.size();
			totalRecs = totalRecs / (float)p.size();	
			totalF1 = (2 * totalPrec * totalRecs)/(totalRecs + totalPrec) ;
			System.out.println( "cat = " + convert(cat) + "\t"+ dec.format(totalPrec) + "\t" 
					+dec.format(totalRecs) + "\t" +dec.format(totalF1) );
			
			macroPrecision += totalPrec ;
			macroRecall += totalRecs ;

		}
		
		//macro calculation //
		macroPrecision /= (categories.size());
		macroRecall	/= (categories.size());
		macroF1 =  (2 * macroPrecision * macroRecall)/(macroRecall + macroPrecision) ;
		System.out.println( "macro avg " + dec.format(macroPrecision) + "\t" 
				+dec.format(macroRecall) + "\t" +dec.format(macroF1) );
		
		//micro calculation//
		microPrecision = microCorrect/microPredict ;
		microRecall = microCorrect/microInput ;
		microF1 = (2*microPrecision*microRecall)/(microPrecision +microRecall ) ;
		
		System.out.println( "micro avg " + dec.format(microPrecision*100) + "\t" 
				+dec.format(microRecall*100) + "\t" +dec.format(microF1*100) );
		
	}

    private String convert(Double cat) 
    {
		// TODO Auto-generated method stub
    	if(cat.doubleValue() == 0.0)
    	{
    		return "NON_SARCASM" ;
    	}
    	if(cat.doubleValue() == 1.0)
    	{
    		return "SARCASM" ;
    	}
		return null;
	}

	public static void main( String[] args ) throws IOException
    {
        // TODO Auto-generated method stub
     // TODO Auto-generated method stub
        String dirPath = "./data/oldpython/";
       // String opFolder = "output/urdu-0222-emotion-sk/";
        
        
     //   String folds [] = {"one","two","three"} ;
        String folds [] = {"one","two","three","four","five"} ;
    //  String folds [] = {"five"} ;
        
        String relation = args[0];
       
        String fileNm = relation + ".01072014.svm.sn.";
        String dataFolder = "5fold/" ;
        String extn = ".op";
        
        int catNum = 2;
        PRF1CalculatorForClassifier prf1CalcObj  = new PRF1CalculatorForClassifier (catNum);
        
   //     prf1CalcObj.calcPRF1ViaFolds(folds, relation, dirPath, dataFolder, fileNm, extn);
        
   
        //single file
        prf1CalcObj.calcPRF1ViaTrainTestSplit() ;
        
    }

	private void calcPRF1ViaTrainTestSplit() throws IOException 
	{
		// TODO Auto-generated method stub
		int svm_type = LIBSVM ; 
		init() ;
		loadSingleOutputFile();
		computePerformance(svm_type);
    	free();
    	printPerformance(svm_type);
    
	
		
	}
	
	

	private void calcPRF1ViaFolds(String[] folds, String relation, String dirPath, 
			String dataFolder, String fileNm, String extn) throws IOException 
	{
		// TODO Auto-generated method stub
		int svm_type = LIBSVM ; 
		relation = "SARCASM" ;
		for ( String fold : folds )
        {
        	String opFolder = "output/" + relation+"/" ;
        	opFolder += fold +"/";
      //  String opFolder = "output/urdu-0304-arg1-sklk/";
       
        
        //cand.emot.pred.doer.gender.together.012
      //  String fileNm = "Located_In.entitysequences.test";
       // String fileNm = "SVMFeatureFileScale.svm";        
        	init();
        	loadOutputFile(dirPath, opFolder, dataFolder, fileNm, extn,folds);
   
    //    prf1CalcObj.loadOutputFileForNoZero(dirPath, opFolder, dataFolder, fileNm, extn,folds);
   //     prf1CalcObj.printTopDataFromAnyCategory();
        	computePerformance(svm_type);
        	free();
        }
        
		printPerformance(svm_type);
	}

	

}
