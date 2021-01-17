package edu.brandeis.cs12b.pa09;

/* 
 * We're including a lot of import statements for you because several of the classes
 * You'll be using in this assignment have the same names as classes in other
 * packages, and we don't want you to get confused and use the wrong one. 
 * You may not use all of these imports, and you might use some that aren't included
 * here. That's ok!
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import wordcloud.CollisionMode;
import wordcloud.WordCloud;
import wordcloud.WordFrequency;
import wordcloud.bg.RectangleBackground;
import wordcloud.font.scale.LinearFontScalar;

public class TwitterCloud {

	/**
	 * The number of tokens you should extract from tweets
	 */
	private static final int NUMBER_TOKENS = 4000;
	private List<String> tokenizedWords = new LinkedList<String>();
	private Twitter twitter = new Twitter();
	/**
	 * Your main client code should go here.
	 * Decide the parameters you want to collect tweets by here, as well as the filename
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		TwitterCloud tc = new TwitterCloud();
		//You may use any search terms to test your code, but we're defaulting to 
		//"donald" and "trump" because you are guaranteed to get many results!
		tc.makeCloud(new String[] { "donald", "trump" }, "test.png");
	}

	/**
	 * Create a word cloud! Remember to use all the tools available in your libraries,
	 * make good decisions on which collections to use to store your data,
	 * and create additional methods as necessary. Use the PA PDF as a guide on how 
	 * to use the various libraries to solve the problem.
	 * 
	 * You can use the code included in Provided_PA09.txt, located in the
	 * root directory of this project, to start things off.
	 * However, you will need to break it up into different methods and classes.
	 * 
	 * Make sure you understand how each part works, and be prepared to explain
	 * your work to your grading TA.
	 * 
	 * Remember: The tests for this PA are very basic, so don't worry about edge cases
	 * or handling bad inputs, etc. Just make your code work, keep it organized,
	 * and be creative!
	 * 
	 * @param args an array of strings to use as a filter for incoming Tweets
	 * @param filename the filename of the image file you should create for your word cloud
	 * @throws InterruptedException 
	 */
	public void makeCloud(String[] args, String filename) throws InterruptedException {

		List<String> tweets = new LinkedList<String>();
		tweets = this.twitter.getTwitters(args, NUMBER_TOKENS);
		System.out.print(tweets);
		this.tokenizedWords = stringToStem(tweets.toString());
		List<WordFrequency> wordFrequency = getWordFrequency(getFrequency(tokenizedWords)); 
		createWordCloud(wordFrequency);
		
	}
	

	/**
	 * This method covert tokenizedWords obtained from Twitter to a List of stems using Apache Lucene
	 * @param tweet
	 * @return a list of tokenized words parsed from Twitter
	 */
	public List<String> stringToStem(String tweet) {
		List<String> tokenizedWords = new LinkedList<String>();
		try (EnglishAnalyzer an = new EnglishAnalyzer()) {
			TokenStream sf = an.tokenStream(null, tweet);
			try {
				sf.reset();
				while (sf.incrementToken()) {
					CharTermAttribute cta = sf.getAttribute(CharTermAttribute.class);
					//Add finished tokens to the list.
					tokenizedWords.add(cta.toString());
				}
			} catch (Exception e) {
				System.err.println("Could not tokenize string: " + e);
			}
		}
		return tokenizedWords;
	}
	

	
	/**
	 * This method return a map which contains each tokenized word with the times it occured
	 * @param tokens
	 * @return a Map of tokenized words with each of its frequency
	 */
	public Map<String, Integer> getFrequency(List<String> tokens) {
		Map<String, Integer> frequency = new HashMap<String, Integer>();
		Iterator<String> iterator = tokens.iterator();
		while(iterator.hasNext()) {
			String word = iterator.next().toString();
			if(frequency.containsKey(word)) {
				frequency.put(word, frequency.get(word)+1);
			}else {
				frequency.put(word, 1);
			}
		}
		return frequency;
	}
	
	
	/**
	 * This method converts a map containing each tokenized word with its frequency into a List<WordFrequency>
	 * @param frequency
	 * @return
	 */
	public List<WordFrequency> getWordFrequency(Map<String, Integer> frequency) {
		List <WordFrequency> wordFrequency = new LinkedList<WordFrequency>();
		Iterator<String> iterator = frequency.keySet().iterator();
		while (iterator.hasNext()){
			String word = iterator.next().toString();
			Integer numOftimes = (Integer) frequency.get(word);
			WordFrequency temp = new WordFrequency(word, numOftimes); 
			wordFrequency.add(temp);
		}	
		return wordFrequency;
	}
	
	
	
	/**
	 * This method use a List of wordFrequenct to generate a photo of words Cloud
	 * @param wordFrequency
	 */
	public void createWordCloud(List<WordFrequency> wordFrequency) {
		WordCloud wordCloud = new WordCloud(400, 400, CollisionMode.RECTANGLE);
		wordCloud.setPadding(0);
		wordCloud.setBackground(new RectangleBackground(400, 400));
		wordCloud.setFontScalar(new LinearFontScalar(14, 40));
		wordCloud.build(wordFrequency);
		wordCloud.writeToFile("test.png");
	}
	

	/**
	 * This method will only be called after makeCloud
	 * @return a list of all tokenized words from your word cloud
	 */
	public List<String> getWords(){
		return this.tokenizedWords;
	}
}
