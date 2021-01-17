package edu.brandeis.cs12b.pa09;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class Twitter {
	private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
	private List<String> tokens = new LinkedList<String>();
	/**
	 * This method extract real-time Twitters from the Twitter app online
	 * @param args
	 * @return a list of elements extracted from many real-time tweets from Twitter
	 * @throws InterruptedException
	 */
	public List<String> getTwitters(String[] args, int NUMBER_TOKENS) throws InterruptedException {
		turnOffPrintingDebuggingInfo();
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		//obtains terms that you want to search	
		List<String> terms = new ArrayList<String>();
		for(String each: args) {
			terms.add(each);	
		}
		hosebirdEndpoint.trackTerms(terms);
		Authentication hosebirdAuth = addAuthentication();
		Client hosebirdClient = connectToTwitter(hosebirdHosts, hosebirdAuth, hosebirdEndpoint);
		this.tokens = getToken(NUMBER_TOKENS);
		hosebirdClient.stop();
		System.out.print(tokens.toString());
		return tokens;
	}
	
	
	/**
	 * This method collect enough tokens
	 * @param NUMBER_TOKENS
	 * @return tokens
	 * @throws InterruptedException
	 */
	public List<String> getToken(int NUMBER_TOKENS) throws InterruptedException{
		//Until we've gather enough tokens
		while (this.tokens.size() < NUMBER_TOKENS){
			String msg = this.msgQueue.take();
			JSONObject temp = new JSONObject(msg);
			if(temp.has("text")) {
				tokens.add(temp.get("text").toString());
			}
		}
		return tokens;
	}
	
	
	
	
	
	
	
	/**
	 * This method use a loop to turn off printing out debugging information
	 */
	public void turnOffPrintingDebuggingInfo() {
		BasicConfigurator.configure();
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());

		loggers.add(LogManager.getRootLogger());
		for ( Logger logger : loggers ) {
			logger.setLevel(Level.OFF);
		}
	}
	/*
	public void DirectHoseBird() {
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
	}
	*/
	
	/**
	 * This method creates authentication so that the application is allowed to have access to real-time Twitters
	 * @return hosebridAuth, an authentication
	 */
	public Authentication addAuthentication() {
		Authentication hosebirdAuth = new OAuth1(
				System.getenv("CONSUMER_KEY"),
				System.getenv("CONSUMER_SECRET"),
				System.getenv("TOKEN"),
				System.getenv("TOKEN_SECRET"));
		return hosebirdAuth;
	}
	
	/**
	 * This method connect the application to Twitter
	 * @param hosebirdHosts
	 * @param hosebirdAuth
	 * @param hosebirdEndpoint
	 * @param msgQueue
	 * @return hosebirdClient
	 */
	public Client connectToTwitter(Hosts hosebirdHosts, Authentication hosebirdAuth,StatusesFilterEndpoint hosebirdEndpoint) {
		ClientBuilder builder = new ClientBuilder()
				.hosts(hosebirdHosts)
				.authentication(hosebirdAuth)
				.endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(this.msgQueue));

		Client hosebirdClient = builder.build();
		hosebirdClient.connect();
		return hosebirdClient;
	}
	
	
	
	
	
	
	
	
	
	
	
}