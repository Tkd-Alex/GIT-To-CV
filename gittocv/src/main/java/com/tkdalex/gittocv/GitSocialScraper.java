package com.tkdalex.gittocv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

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
	
	public HashMap<String, String> getInfo(String email){
		if(this.socialname.equals("github")) {
			JSONObject users_result, fullinfo;
			try {
				// #1 - Search user by email
				users_result = this.makeRequest( String.format("https://api.github.com/search/users?q=%s+in:email", email) );
				if(users_result != null && users_result.getJSONArray("items").length() != 0 ){
					HashMap<String, String> info = new HashMap<String, String>();
					
					JSONObject user = (JSONObject) users_result.getJSONArray("items").get(0) ;
					String socialid = user.get("id").toString();
					
					info.put("id", user.get("id").toString());
					info.put("username", (String) user.get("login"));
					info.put("avatar_url", (String) user.get("avatar_url"));
					
					// #2 - Get full info by id
					fullinfo = this.makeRequest( "https://api.github.com/user/" + socialid );
					if(fullinfo!= null) {
						info.put("website", (String) fullinfo.get("blog"));
						info.put("location", (fullinfo.isNull("location")) ? null : (String) fullinfo.get("location"));
						info.put("bio", (fullinfo.isNull("bio")) ? null : (String) fullinfo.get("bio"));
						info.put("created_at", (String) fullinfo.get("created_at"));
					return info;
					}
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
		else if(this.socialname.equals("bitbucket")) {
			JSONObject fullinfo;
			try {
				// #1 - Search user by email
				fullinfo = this.makeRequest( "https://api.bitbucket.org/2.0/users/" + email );
				if(fullinfo != null ){
					HashMap<String, String> info = new HashMap<String, String>();
					info.put("id", (String) fullinfo.get("account_id"));
					info.put("username", (String) fullinfo.get("nickname"));
					
					JSONObject links = (JSONObject) fullinfo.get("links");
					String avatarl_url = (String) ((JSONObject) links.get("avatar") ).get("href");
					info.put("avatar_url", avatarl_url);
					
					info.put("website", null);
					info.put("location", null);
					info.put("bio", null);
					info.put("created_at", (String) fullinfo.get("created_on"));
					return info;
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
		else if(this.socialname.equals("gitlab")) {
			return null; // auth required for GitLab :(
			
			/*
			JSONObject users_result, fullinfo;
			try {
				// #1 - Search user by email
				users_result = this.makeRequest( "https://gitlab.com/api/v4/users?search=" + email );
				if(users_result != null && users_result.getJSONArray("").length() != 0 ){
					JSONObject user = (JSONObject) users_result.getJSONArray("").get(0) ;
					String socialid = user.get("id").toString();
					
					// #2 - Get full info by id
					fullinfo = this.makeRequest( "https://gitlab.com/api/v4/users/" + socialid );
					if(fullinfo!= null) {
						HashMap<String, String> info = new HashMap<String, String>();
						info.put("id", fullinfo.get("id").toString());
						info.put("username", (String) fullinfo.get("username"));
						info.put("avatar_url", (String) fullinfo.get("avatar_url"));
						info.put("website", (String) fullinfo.get("website_url"));
						info.put("location", (String) fullinfo.get("location"));
						info.put("bio", (String) fullinfo.get("bio"));
						info.put("created_at", (String) fullinfo.get("created_at"));
						return info;
					}
				}
			} catch (Exception e) { e.printStackTrace(); }
			*/
		}
		return null;
	}
}
