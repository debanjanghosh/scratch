package nlp.assignments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nlp.assignments.LanguageModelTester.EditDistance;
import nlp.util.CommandLineUtils;

public class Homework1Evaluator {

	private static List<String> readSentence(String line) {
		List<String> sentence;
		String[] words = line.substring(line.indexOf('[') + 1).split(",");
		return Arrays.asList(words);
	}
	
	public static void main(String[] args) throws IOException {
		// Parse command line flags and arguments
		Map<String, String> argMap = CommandLineUtils.simpleCommandLineParser(args);

		// The path to the assignment data
		String fileName = "";
		if (argMap.containsKey("-path")) {
			fileName = argMap.get("-path");
		}

	    double totalDistance = 0.0;
	    double totalWords = 0.0;
	    EditDistance editDistance = new EditDistance();

	    String line = "";
        List<String> correctSentence, guess = null;
	    BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {
        	if (line == "") continue;
        	else if (line.startsWith("GUESS")) {
        		guess = readSentence(line);
        		continue;
        	}
        	else if (line.startsWith("GOLD")) {
        		correctSentence = readSentence(line);
        		totalDistance += editDistance.getDistance(correctSentence, guess);
        		totalWords += correctSentence.size();
        	}
        }
		System.out.printf("%2.2f", totalDistance / totalWords * 100);
	}
}
