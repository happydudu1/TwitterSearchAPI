package com.twitter.search.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class TwitterSearch {
	private final static Logger log = Logger.getLogger(TwitterSearch.class);
	
	private final String CONSUMER_KEY="3GqepHyVqOa72qKNl7vIMccip";
	private final String CONSUMER_SECRET="WUqXaXuWqgO8NPeL3x6Z21GUlpMxXuZtrJeWqVp9R39CO2kcEO";
	private final String USER_AGENT="testSearchAPI2015";
	
	/**
	 * Encodes the consumer key and secret to create the basic authorization key
	 * @param consumerKey
	 * @param consumerSecret
	 * @return String
	 */
	private String encodeKeys(String consumerKey, String consumerSecret) {
		try {
			String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
			String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
			
			String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
			byte[] encodedBytes = Base64.encodeBase64(fullKey.getBytes());
			return new String(encodedBytes);  
		}
		catch (UnsupportedEncodingException e) {
			return new String();
		}
	}
	
	/**
	 * Constructs the request for requesting a bearer token and returns that token as a string
	 * @param endPointUrl
	 * @return
	 * @throws IOException
	 */
	private String requestBearerToken(String endPointUrl) throws IOException {
		HttpsURLConnection connection = null;
		String encodedCredentials = encodeKeys(CONSUMER_KEY,CONSUMER_SECRET);
			
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Host", "api.twitter.com");
		    connection.setRequestProperty("User-Agent", USER_AGENT);
			
			connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); 
			connection.setRequestProperty("Content-Length", "29");
			connection.setUseCaches(false);
				
			writeRequest(connection, "grant_type=client_credentials");
				
			// Parse the JSON response into a JSON mapped object to fetch fields from.
			JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
			 
			if (obj != null) {
				String tokenType = (String)obj.get("token_type");
				String token = (String)obj.get("access_token");
			
				return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
			}else{
				 
	            if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
	                log.error("HTTP 403 (Forbidden) returned from Twitter API call for bearer token. Check values of Consumer Key and Consumer Secret in tokens.properties");
	            }
			}
			return new String();
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	
	/**
	 * fetch result 
	 * @param endPointUrl
	 * @throws IOException
	 */
	private List<String> getSearchResult(String endPointUrl, String bearerToken) throws IOException {
		HttpsURLConnection connection = null;
		List<String> res=new ArrayList<String>();			
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
      	    connection.setRequestProperty("User-Agent", USER_AGENT);
			 
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setUseCaches(false);
				
	 
			 JSONArray arr = (JSONArray) ((JSONObject)JSONValue.parse(readResponse(connection))).get("statuses");
	 
			if (arr!=null){ 
				for(int i=0;i<arr.size();i++)
				{
					String loc=(String)(( (JSONObject)((JSONObject)arr.get(i)).get("user")).get("location"));
					if (loc!=null&&!loc.equals("")){
						res.add(loc);
					}
				}		 
			} 
			return res;
		 
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Writes a request to a connection
	 * @param connection
	 * @param textBody
	 * @return
	 */
	private boolean writeRequest(HttpsURLConnection connection, String textBody) {
		try {
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();
				
			return true;
		}
		catch (IOException e) { return false; }
	}
		
		
	/**
	 * Reads a response for a given connection and returns it as a string.
	 * @param connection
	 * @return
	 */
	private static String readResponse(HttpsURLConnection connection) {
		try {
			StringBuilder str = new StringBuilder();
				
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		 	String line = "";
			while((line = br.readLine()) != null) {
				str.append(line + System.getProperty("line.separator"));
			}
			return str.toString();
		}
		catch (IOException e) { return new String(); 
		}
	 }
	
		public static void main( String[] args ){
			final String endPointUrl="https://api.twitter.com/oauth2/token";
			
			TwitterSearch ts = new TwitterSearch();
			
			try {
				System.out.println("Enter Celbrity Name: ");
				
				Scanner in = new Scanner(System.in);
				String line = in.nextLine();  
				String url = "https://api.twitter.com/1.1/search/tweets.json?q=" + URLEncoder.encode(line, "UTF-8");
		 
				List<String> res= ts.getSearchResult(url, ts.requestBearerToken(endPointUrl));
				if (res.size()>0){
					System.out.println("USER LOCATIONS:");
					System.out.println("===========================");
					for(String l:res){
						System.out.println(l);
					}
				}else{
					System.out.println("NO USER SEARCHED: "+ line);
				}
				
			} catch (IOException e) {
				 
				e.printStackTrace();
			}
		 
    }
}
