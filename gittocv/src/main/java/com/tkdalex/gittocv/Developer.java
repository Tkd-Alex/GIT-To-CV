package com.tkdalex.gittocv;

import java.util.HashMap;

public class Developer { // Should be committer
	public String name;
	public String email;
	public int commit;
	
	private String id;
	private String username;
	private String avatar_url;
	private String website;
	private String location;
	private String bio;
	private String created_at;
	
	private HashMap<String, Integer> devPoints;
	
	// https://api.github.com/users/tkd-alex
	public Developer(String name, String email) {
		this.name = name;
		this.email = email;
		this.commit = 0;
		this.devPoints = new HashMap<String, Integer>();
		this.devPoints.put("backend", 0);
		this.devPoints.put("frontend", 0);
		this.devPoints.put("writer", 0);
		
		this.setId(null);
		this.setUsername(null);
		this.setAvatar_url(null);
		this.setWebsite(null);
		this.setLocation(null);
		this.setBio(null);
		this.setCreated_at(null);
	}
	
	public void initSocialInfo(String socialname) {
		GitSocialScraper socialscraper = new GitSocialScraper(socialname);
		try {
			HashMap<String, String> socialinfo = socialscraper.getInfo(this.email);
			if(socialinfo != null) {
				this.setId(socialinfo.get("id"));
				this.setUsername(socialinfo.get("username"));
				this.setAvatar_url(socialinfo.get("avatar_url"));
				this.setWebsite(socialinfo.get("website"));
				this.setLocation(socialinfo.get("location"));
				this.setBio(socialinfo.get("bio"));
				this.setCreated_at(socialinfo.get("created_at"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Integer editPoints(String key, Integer value) {
		this.devPoints.put(key, this.devPoints.get(key) + value);
		return this.devPoints.get(key);
	}
	
	public Integer getPoints(String key) {
		return this.devPoints.get(key);
	}
	
	public void print() {
		System.out.println("Name: " + this.name);
		System.out.println("Email: " + this.email);
		// System.out.println("Commit: " + this.commit);
		
		Integer total = 0;
		for (String i : this.devPoints.keySet()) {
			// System.out.println("Category: " + i + " Points: " + this.devPoints.get(i));
			total += this.devPoints.get(i);
		}
		if(total != 0)
			for (String i : this.devPoints.keySet())
				System.out.println("Category: " + i + " Percentage: " + Math.round( ((float)this.devPoints.get(i)*100)/total ) + "%" );
		
		/*
		if(this.id 			!= null) System.out.println(this.id);
		if(this.username 	!= null) System.out.println(this.username);
		if(this.avatar_url 	!= null) System.out.println(this.avatar_url);
		if(this.website 	!= null) System.out.println(this.website);
		if(this.location 	!= null) System.out.println(this.location);
		if(this.bio 		!= null) System.out.println(this.bio);
		if(this.created_at 	!= null) System.out.println(this.created_at);
		*/
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getAvatar_url() { return avatar_url; }
	public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

	public String getWebsite() { return website; }
	public void setWebsite(String website) { this.website = website; }

	public String getLocation() { return location; }
	public void setLocation(String location) { this.location = location; }

	public String getBio() { return bio; }
	public void setBio(String bio) { this.bio = bio; }

	public String getCreated_at() { return created_at; }
	public void setCreated_at(String created_at) { this.created_at = created_at; }

}
