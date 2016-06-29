package nlp.assignments.lm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nlp.langmodel.LanguageModel;
import nlp.util.Counter;
import nlp.util.CounterMap;

/**
 * A dummy language model -- uses empirical unigram counts, plus a single
 * ficticious count for unknown words.
 */
public class EmpiricalBigramLanguageModelWithEM implements LanguageModel {

	static final String START = "<S>";
	static final String STOP = "</S>";
	static final String UNKNOWN = "*UNKNOWN*";
	static final double lambda = 0.6;

	Counter<String> wordCounter = new Counter<String>();
	CounterMap<String, String> bigramCounter = new CounterMap<String, String>();

	Counter<String> wordValidCounter = new Counter<String>();
	CounterMap<String, String> bigramValidCounter = new CounterMap<String, String>();

	
	public void calculateEM ( Collection<List<String>> sentences )
	{
		double lambda_1 = 0.1 ; 
		double lambda_2 = 0.9 ; //lambda1 and lambda2 >=0 and lambda1 + lambda2 = 1 
		
		//we need to maximize the log-likelihood of the main LM with taking
		//counts of validation model (for each bigram and unigram)
		//do it for all sentences till we get maximized log
		
		double c1 = 0 ;
		double c2 = 0 ;
		
		double maxmaxLoglike = Double.MIN_VALUE ;
		
		while (true)
		{
		
			for ( List<String> sentence : sentences )
			{
				List<String> words = new ArrayList<String> (sentence) ;
				words.add(0, START);
				words.add(STOP);
			
				String previousWord = words.get(0); //first word
				double probability = 1.0;
			
				for (int i = 1; i < words.size(); i++) 
				{
					String word = words.get(i);
					double bigramProb = getOnlyBigramProbability(previousWord, word);
					double unigramProb = getOnlyUnigramProbability(word);
				
					double bigramValidCount = getOnlyValidBigramProbability(previousWord, word);
				
					//c1 = sigma ( c'(w1|w2) * lambda_1 * ML ( bigramProb ) / lambda_1 * ML ( bigramProb ) + lambda_2 * ML ( unigramProb )
				
					c1 +=  ( bigramValidCount * lambda_1 * bigramProb ) / (lambda_1 * bigramProb + lambda_2 * unigramProb) ;
					c2 +=  ( bigramValidCount * lambda_2 * unigramProb ) / (lambda_1 * bigramProb + lambda_2 * unigramProb) ;
			
					previousWord = word;
				}
			}
		
			lambda_1 = c1 / (c1+c2) ;
			lambda_2 = c2 / (c1+c2) ;
			
			//now calculate the log-likelihood of the original ML Language Model equation
			double maxLikelihood = computeMaximumLogLikelihood(sentences,lambda_1,lambda_2) ;
			
			double min = Math.min(maxLikelihood, maxmaxLoglike);
			
		//	if(maxLikelihood > maxmaxLoglike)
			System.out.println("new value of lambda_1" + " " + lambda_1) ;
			System.out.println("new value of lambda_2" + " " + lambda_2) ;
			System.out.println("maxlikelihood is " + maxLikelihood) ;
			
		}
	}
	
	public double computeMaximumLogLikelihood(Collection<List<String>> sentences, 
			double lambda_1, double lambda_2)
	{
		//from MC's note:
		//L (lambda_1, lambda_2) = sum (c' (w1,w2) log (LM(w1,w2)))
			
		double logLikeSum = 0 ;
		
		for ( List<String> sentence : sentences )
		{
			List<String> words = new ArrayList<String> (sentence) ;
			words.add(0, START);
			words.add(STOP);
			
			String previousWord = words.get(0); //first word
			double probability = 1.0;
			
			for (int i = 1; i < words.size(); i++) 
			{
				String word = words.get(i);
				double bigramProb = getBigramProbability(previousWord, word,lambda_1,lambda_2);
				bigramProb = Math.log(bigramProb);
				double bigramValidCount = getOnlyValidBigramProbability(previousWord, word);
				
				logLikeSum += bigramValidCount * bigramProb ;
				
				previousWord = word;
				
			}
		}
		
		return logLikeSum ;
	}
	
	
	
	public double getOnlyUnigramProbability( String word) 
	{
		double unigramProb = wordCounter.getCount(word);
		if (unigramProb == 0) 
		{
	//		System.out.println("UNKNOWN Word: " + word);
			unigramProb = wordCounter.getCount(UNKNOWN);
		}
		return unigramProb;
	}
	
	
	public double getOnlyBigramProbability(String previousWord, String word) 
	{
		double bigramProb = bigramCounter.getCount(previousWord, word);
		return bigramProb;
	}
	
	public double getOnlyValidUnigramProbability( String word) 
	{
		double unigramProb = wordValidCounter.getCount(word);
		if (unigramProb == 0) 
		{
	//		System.out.println("UNKNOWN Word: " + word);
			unigramProb = wordValidCounter.getCount(UNKNOWN);
		}
		return unigramProb;
	}
	
	
	public double getOnlyValidBigramProbability(String previousWord, String word) 
	{
		double bigramProb = bigramValidCounter.getCount(previousWord, word);
		return bigramProb;
	}
	
	
	public double getBigramProbability(String previousWord, String word) 
	{
		double bigramCount = bigramCounter.getCount(previousWord, word);
		double unigramCount = wordCounter.getCount(word);
		if (unigramCount == 0) 
		{
			System.out.println("UNKNOWN Word: " + word);
			unigramCount = wordCounter.getCount(UNKNOWN);
		}
		return lambda * bigramCount + (1.0 - lambda) * unigramCount;
	}

	public double getBigramProbability(String previousWord, String word, double lambda_1, double lambda_2) 
	{
		double bigramCount = bigramCounter.getCount(previousWord, word);
		double unigramCount = wordCounter.getCount(word);
		if (unigramCount == 0) 
		{
	//		System.out.println("UNKNOWN Word: " + word);
			unigramCount = wordCounter.getCount(UNKNOWN);
		}
		return lambda_1 * bigramCount + lambda_2 * unigramCount;
	}

	
	public double getSentenceProbability(List<String> sentence) 
	{
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(0, START);
		stoppedSentence.add(STOP);
		double probability = 1.0;
		String previousWord = stoppedSentence.get(0);
		for (int i = 1; i < stoppedSentence.size(); i++) {
			String word = stoppedSentence.get(i);
			probability *= getBigramProbability(previousWord, word);
			previousWord = word;
		}
		return probability;
	}

	String generateWord() 
	{
		double sample = Math.random();
		double sum = 0.0;
		for (String word : wordCounter.keySet()) {
			sum += wordCounter.getCount(word);
			if (sum > sample) {
				return word;
			}
		}
		return UNKNOWN;
	}

	public List<String> generateSentence() {
		List<String> sentence = new ArrayList<String>();
		String word = generateWord();
		while (!word.equals(STOP)) {
			sentence.add(word);
			word = generateWord();
		}
		return sentence;
	}

	public EmpiricalBigramLanguageModelWithEM(
			Collection<List<String>> sentenceCollection) {
		for (List<String> sentence : sentenceCollection) {
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, START);
			stoppedSentence.add(STOP);
			String previousWord = stoppedSentence.get(0);
			for (int i = 1; i < stoppedSentence.size(); i++) {
				String word = stoppedSentence.get(i);
				wordCounter.incrementCount(word, 1.0);
				bigramCounter.incrementCount(previousWord, word, 1.0);
				previousWord = word;
			}
		}
		wordCounter.incrementCount(UNKNOWN, 1.0);

		// double check = wordCounter.getCount(UNKNOWN) ;

		normalizeDistributions();
	}

	public EmpiricalBigramLanguageModelWithEM(
			Collection<List<String>> sentenceCollection,
			Collection<List<String>> validationCollection) 
	{
		//first load the training LM
		for (List<String> sentence : sentenceCollection) 
		{
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, START);
			stoppedSentence.add(STOP);
			String previousWord = stoppedSentence.get(0);
			for (int i = 1; i < stoppedSentence.size(); i++) 
			{
				String word = stoppedSentence.get(i);
				wordCounter.incrementCount(word, 1.0);
				bigramCounter.incrementCount(previousWord, word, 1.0);
				previousWord = word;
			}
		}
		wordCounter.incrementCount(UNKNOWN, 1.0);
		normalizeDistributions();
		
		//now load the validation objects for counts C1 and C2 for smoothing the lambdas
		for (List<String> sentence : validationCollection) 
		{
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, START);
			stoppedSentence.add(STOP);
			String previousWord = stoppedSentence.get(0);
			for (int i = 1; i < stoppedSentence.size(); i++) 
			{
				String word = stoppedSentence.get(i);
				wordValidCounter.incrementCount(word, 1.0);
				bigramValidCounter.incrementCount(previousWord, word, 1.0);
				previousWord = word;
			}
		}
		wordValidCounter.incrementCount(UNKNOWN, 1.0);
		
		//calculate the EM
		calculateEM(validationCollection) ;
	}

	private void normalizeDistributions() {
		for (String previousWord : bigramCounter.keySet()) {
			bigramCounter.getCounter(previousWord).normalize();
		}
		wordCounter.normalize();
	}
}
