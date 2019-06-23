package com.tkdalex.gittocv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class GitSocialScraper {
	
	public String socialname;
	
	public GitSocialScraper(String socialname) {
		this.socialname = socialname;
	}
	
	public JSONObject makeRequest(String url_string) throws Exception {
		URL obj = new URL( url_string );
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		// System.out.println("\nSending 'GET' request to URL : " + url_string);
		int responseCode = con.getResponseCode();
		// System.out.println("Response Code : " + responseCode);
		if(responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// System.out.println(response.toString());
			JSONObject myResponse = new JSONObject(response.toString());
			return myResponse;
		}
		return null;
	}
	
	public JSONObject getInfo(String email){
		if(this.socialname.equals("github")) {
			// #1 - Search user by email
			JSONObject users_result;
			try {
				users_result = this.makeRequest( String.format("https://api.github.com/search/users?q=%s+in:email", email) );
				JSONObject user = (JSONObject) users_result.getJSONArray("items").get(0) ;
				System.out.println(user.get("login"));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// JSONObject user = (JSONObject) users_result.get("items");
			//String username = user.getString("username");
			
			// #2 - Get full info by username
		}
		 return null;
	}
}
