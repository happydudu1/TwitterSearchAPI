package twitterSearch.searchAPI;

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
import java.util.ListIterator;
import java.util.Scanner;

import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Hello world!
 *
 */
public class TwitterSearch 
{
	private static Logger log = Logger.getLogger(TwitterSearch.class);
	
	public static String consumerKey="3GqepHyVqOa72qKNl7vIMccip";
	public static String consumerSecret="WUqXaXuWqgO8NPeL3x6Z21GUlpMxXuZtrJeWqVp9R39CO2kcEO";
	public static String bearerToken="";
	public static String userAgent="testSearchAPI2015";
			
 
	// Encodes the consumer key and secret to create the basic authorization key
	private static String encodeKeys(String consumerKey, String consumerSecret) {
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
	
	// Constructs the request for requesting a bearer token and returns that token as a string
	private static String requestBearerToken(String endPointUrl) throws IOException {
		HttpsURLConnection connection = null;
		String encodedCredentials = encodeKeys(consumerKey,consumerSecret);
			
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Host", "api.twitter.com");
		    connection.setRequestProperty("User-Agent", userAgent);
			
			connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); 
			connection.setRequestProperty("Content-Length", "29");
			connection.setUseCaches(false);
				
			writeRequest(connection, "grant_type=client_credentials");
				
			// Parse the JSON response into a JSON mapped object to fetch fields from.
			JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
			log.debug("json Response of the bearer oauth request : " + obj);
	           
    		
			if (obj != null) {
				String tokenType = (String)obj.get("token_type");
				String token = (String)obj.get("access_token");
			
				return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
			}else{
				 
	            if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
	                log.error("HTTP 403 (Forbidden) returned from Twitter API call for bearer token. Check values of Consumer Key and Consumer Secret in tokens.properties");
	               // throw new RejectedAuthorizationException("","HTTP 403 (Forbidden) returned attempting to get Twitter API bearer token");
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
	
	//fetch result 
	private static void getAndPrintSearchResult(String endPointUrl) throws IOException {
		HttpsURLConnection connection = null;
					
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
      	    connection.setRequestProperty("User-Agent", userAgent);
			 
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setUseCaches(false);
				
	 
			 JSONArray arr = (JSONArray) ((JSONObject)JSONValue.parse(readResponse(connection))).get("statuses");
	 
			if (arr!=null){ 
				for(int i=0;i<arr.size();i++)
				{
					String loc=(String)(( (JSONObject)((JSONObject)arr.get(i)).get("user")).get("location"));
					if (loc!=null&&!loc.equals("")){
						System.out.println(loc);
					}
				}		 
			}else{
				System.out.println("no result found!");
			} 
		 
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
	// Writes a request to a connection
	private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
		try {
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();
				
			return true;
		}
		catch (IOException e) { return false; }
	}
		
		
	// Reads a response for a given connection and returns it as a string.
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
			String endPointUrl="https://api.twitter.com/oauth2/token";
			try {
				bearerToken=requestBearerToken(endPointUrl);
			 
				Scanner in = new Scanner(System.in);

				System.out.println("Enter Celbrity Name: ");
				String line = in.nextLine();  
				String url = "https://api.twitter.com/1.1/search/tweets.json?q=" + URLEncoder.encode(line, "UTF-8");
		 
				getAndPrintSearchResult(url);
			} catch (IOException e) {
				 
				e.printStackTrace();
			}
		 
    }
}
