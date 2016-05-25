package com.wsd.kernel ;

import libsvm.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.wsd.util.PRF1Measure;
import com.wsd.util.RefTargetPair;


public class svm_predict 
{
	private Map<Double, List<RefTargetPair>> eachCategoryMap;
	private PRF1Measure PRF1MeasureObject  = new PRF1Measure();
	private svm_model trainingModel;
	private int predict_probability  = 0; //by default no probability
	private static String vocabFile ;
	private Set<String> vocabs ;
	private static double context ;

	private static svm_print_interface svm_print_null = new svm_print_interface()
	{
		public void print(String s) {}
	};

	private static svm_print_interface svm_print_stdout = new svm_print_interface()
	{
		public void print(String s)
		{
			System.out.print(s);
		}
	};

	private static svm_print_interface svm_print_string = svm_print_stdout;

	static void info(String s) 
	{
		svm_print_string.print(s);
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}
	
	public void setTrainingModel ( svm_model model)
	{
		this.trainingModel = model ;
	}
	
	public void setProbabilityType ( int probType)
	{
		this.predict_probability   = probType ;
	}
	
	public double predict_context ( String message, String category )
	{
		int svm_type=svm.svm_get_svm_type(trainingModel);
		int nr_class=svm.svm_get_nr_class(trainingModel);
	
		if ( context == 2.0)
		{
			String features[] = message.split("\t");
			message = features[0] + " " + features[1].split("\\|\\|\\|")[0] + " " + "|||" + " " + features[1].split("\\|\\|\\|")[1] ;
		}

		else if ( context == 1.0)
		{
			String features[] = message.split("\t");
			message = features[0] + " " + features[1].split("\\|\\|\\|")[0] + " " + features[1].split("\\|\\|\\|")[1] ;
		}
		else
		{
			String features[] = message.split("\t");
			message = features[0] + " " + features[1].split("\\|\\|\\|")[0] ;

		}
		
		String[] tokens = message.split("\\s++") ;
		double target = atof(tokens[0]);
		
		int m = tokens.length-1;
		wsd_node[] x = new wsd_node[m];
		double[] prob_estimates=null;

		for(int j=1;j<=m;j++)
		{
			x[j-1] = new wsd_node();
			if(tokens[j].toLowerCase().equalsIgnoreCase(category) ||
					tokens[j].toLowerCase().equalsIgnoreCase("#"+category) )
			{
				continue ;
			}
			
			x[j-1].token = tokens[j];
		}
		
		double v;
		if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
		{
			v = svm.svm_predict_probability(trainingModel,x,prob_estimates,wordEmbeddingObject);
	//		output.writeBytes(v+" ");
	//		for(int j=0;j<nr_class;j++)
	//			output.writeBytes(prob_estimates[j]+" ");
	//		output.writeBytes("\n");
		}
		else
		{
			v = svm.svm_predict(trainingModel,x,wordEmbeddingObject);
		}
		
		return v ;
	}
	
	public double predict ( String message, String category )
	{
		int svm_type=svm.svm_get_svm_type(trainingModel);
		int nr_class=svm.svm_get_nr_class(trainingModel);
	
		
		String[] tokens = message.split("\\s++") ;
		double target = atof(tokens[0]);
		
		int m = tokens.length-1;
		wsd_node[] x = new wsd_node[m];
		double[] prob_estimates=null;

		for(int j=1;j<=m;j++)
		{
			x[j-1] = new wsd_node();
			if(tokens[j].toLowerCase().equalsIgnoreCase(category) ||
					tokens[j].toLowerCase().equalsIgnoreCase("#"+category) )
			{
				continue ;
			}
			
			x[j-1].token = tokens[j];
		}
		
		double v;
		if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
		{
			v = svm.svm_predict_probability(trainingModel,x,prob_estimates,wordEmbeddingObject);
	//		output.writeBytes(v+" ");
	//		for(int j=0;j<nr_class;j++)
	//			output.writeBytes(prob_estimates[j]+" ");
	//		output.writeBytes("\n");
		}
		else
		{
			v = svm.svm_predict(trainingModel,x,wordEmbeddingObject);
		}
		
		return v ;
	}



	public void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability,
			String category) throws IOException
	{
		int correct = 0;
		int total = 0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		loadWSDEmbedding() ;
		loadVocab();
		
		List<RefTargetPair> refTargetPairList = null ;
		eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
		RefTargetPair pair = null ;

		while(true)
		{
			String line = input.readLine();
			if(line == null) 
			{
				break;
			}
			
			String[] tokens = line.split("\\s++") ;
			double target = atof(tokens[0]);
			
			int m = tokens.length-1;
			wsd_node[] x = new wsd_node[m];
			for(int j=1;j<=m;j++)
			{
				x[j-1] = new wsd_node();
				if(tokens[j].toLowerCase().equalsIgnoreCase(category) ||
						tokens[j].toLowerCase().equalsIgnoreCase("#"+category) )
				{
					continue ;
				}
				
				if ( !vocabs.contains(tokens[j].toLowerCase()))
				{
					continue ;
				}
				
				x[j-1].token = tokens[j];
			}
			
			
			
			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates,wordEmbeddingObject);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x,wordEmbeddingObject);
				output.writeBytes(v+"\n");
			}

			//for p/r/f1
			refTargetPairList = eachCategoryMap.get(target);
			if ( null == refTargetPairList )
			{
				refTargetPairList = new ArrayList<RefTargetPair>();
			}
	
			pair = new RefTargetPair(target,v);
			refTargetPairList.add(pair);
			eachCategoryMap.put(target, refTargetPairList);
	
			
			
			
			if(v == target)
				++correct;
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
			
			
			
			
		}
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			svm_predict.info("Mean squared error = "+error/total+" (regression)\n");
			svm_predict.info("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else
			svm_predict.info("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
		
		PRF1MeasureObject.setPRParameters(eachCategoryMap) ;
		List<String> ops = PRF1MeasureObject.calculateSeparatePRF1();
		for ( String op : ops )
		{
		/*	
			if ( op.contains("category: 0.0"))
			{
				continue ;
			}
        */
			System.out.println(category + "\t" +total + "\t" + op);
			eachCategoryMap.clear();
		} 
	}

	public void predict_context(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability,
			String category) throws IOException
	{
		int correct = 0;
		int total = 0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		loadWSDEmbedding() ;
		loadVocab();
		
		List<RefTargetPair> refTargetPairList = null ;
		eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
		RefTargetPair pair = null ;

		while(true)
		{
			String line = input.readLine();
			if(line == null) 
			{
				break;
			}
			
			if ( context == 2.0)
			{
				String features[] = line.split("\t");
				line = features[0] + " " + features[1].split("\\|\\|\\|")[0] + " " + "|||" + " " + features[1].split("\\|\\|\\|")[1] ;
			}

			
			else if ( context == 1.0)
			{
				String features[] = line.split("\t");
				line = features[0] + " " + features[1].split("\\|\\|\\|")[0] + " " + features[1].split("\\|\\|\\|")[1] ;
			}
			else
			{
				String features[] = line.split("\t");
				line = features[0] + " " + features[1].split("\\|\\|\\|")[0] ;

			}
		
			
			String[] tokens = line.split("\\s++") ;
			double target = atof(tokens[0]);
			
			int m = tokens.length-1;
			wsd_node[] x = new wsd_node[m];
			for(int j=1;j<=m;j++)
			{
				x[j-1] = new wsd_node();
				if(tokens[j].toLowerCase().equalsIgnoreCase(category) ||
						tokens[j].toLowerCase().equalsIgnoreCase("#"+category) )
				{
					continue ;
				}
				
				if ( !vocabs.contains(tokens[j].toLowerCase()))
				{
					if (!tokens[j].equalsIgnoreCase("|||"))
					{
						continue ;
					}
				}
				
				x[j-1].token = tokens[j];
			}
			
			
			
			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates,wordEmbeddingObject);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x,wordEmbeddingObject);
				output.writeBytes(v+"\n");
			}

			//for p/r/f1
			refTargetPairList = eachCategoryMap.get(target);
			if ( null == refTargetPairList )
			{
				refTargetPairList = new ArrayList<RefTargetPair>();
			}
	
			pair = new RefTargetPair(target,v);
			refTargetPairList.add(pair);
			eachCategoryMap.put(target, refTargetPairList);
	
			
			
			
			if(v == target)
				++correct;
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
			
			
			
			
		}
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			svm_predict.info("Mean squared error = "+error/total+" (regression)\n");
			svm_predict.info("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else
			svm_predict.info("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
		
		PRF1MeasureObject.setPRParameters(eachCategoryMap) ;
		List<String> ops = PRF1MeasureObject.calculateSeparatePRF1();
		for ( String op : ops )
		{
		/*	
			if ( op.contains("category: 0.0"))
			{
				continue ;
			}
        */
			System.out.println(category + "\t" +total + "\t" + op);
			eachCategoryMap.clear();
		} 
	}

	private static void exit_with_help()
	{
		System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
		+"options:\n"
		+"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
		+"-q : quiet mode (no outputs)\n");
		System.exit(1);
	}

	private WordEmbedding wordEmbeddingObject;
	private static String embedPath;
	private static String wordEmFile;
	
	public void loadWSDEmbedding() 
	{
		// TODO Auto-generated method stub
		//for glove
	//	String embedPath = "./data/glove/input/" ;
	//	String wordEmFile = "tweet.all.03222015.tokens.lemma.emoji.wmf.model.bin.txt" ;
	//	wordEmbeddingObject = new WordEmbedding() ;
	//	wordEmbeddingObject.readGloveWE(embedPath+"/"+wordEmFile);
		
		wordEmbeddingObject = new WordEmbedding() ;
		wordEmbeddingObject.readGensimWE(embedPath+"/"+wordEmFile);
		

	}
	
	public void loadVocab() throws IOException
	{
		List<String> lines = Files.readAllLines(Paths.get("./data/config/" + vocabFile), StandardCharsets.UTF_8) ;
		vocabs = new HashSet<String>();
		for ( String line : lines )
		{
			String vocab = line.split("\t")[0].trim().toLowerCase() ;
			vocabs.add(vocab) ;
		}
		
	}

	
	public static void activate ( ) throws IOException
	{
		String configPath = "./data/config/" ;
		String configFile = "sarcasm_context.properties" ;
		Properties prop = new Properties();
		InputStream input = null;
	 		
		input = new FileInputStream(configPath + "/" +configFile);
	 
		// load a properties file
		prop.load(input);
	 
		// get the property value and print it out
		embedPath = prop.getProperty("embedPath");
		wordEmFile = prop.getProperty("wordEmFile") ;
		vocabFile = prop.getProperty("vocabFile") ;

		input.close() ;
	}

	public static void main(String argv[]) throws IOException
	{
		int i, predict_probability=0;
        	svm_print_string = svm_print_stdout;

        String target =null;
		// parse options
		activate();

        
        
		for(i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			++i;
			switch(argv[i-1].charAt(1))
			{
				case 'b':
					predict_probability = atoi(argv[i]);
					break;
				case 'q':
					svm_print_string = svm_print_null;
					i--;
					break;
				case 'f':
					context = atof(argv[i]) ;
					break ;
				case 'a':	
					target = argv[i] ;
					break;
				default:
					System.err.print("Unknown option: " + argv[i-1] + "\n");
					exit_with_help();
			}
		}
		if(i>=argv.length-2)
			exit_with_help();
		try 
		{
			BufferedReader input = new BufferedReader(
				      new InputStreamReader(new FileInputStream(argv[i]), "UTF-8"));

			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
			svm_model model = svm.svm_load_model(argv[i+1]);
			if(predict_probability == 1)
			{
				if(svm.svm_check_probability_model(model)==0)
				{
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			}
			else
			{
				if(svm.svm_check_probability_model(model)!=0)
				{
					svm_predict.info("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			svm_predict predictObj = new svm_predict() ;
			
		//	predictObj.predict(input,output,model,predict_probability,target);
			predictObj.predict_context(input,output,model,predict_probability,target);

			input.close();
			output.close();
		} 
		catch(FileNotFoundException e) 
		{
			exit_with_help();
		}
		catch(ArrayIndexOutOfBoundsException e) 
		{
			exit_with_help();
		}
	}
	
	
}
