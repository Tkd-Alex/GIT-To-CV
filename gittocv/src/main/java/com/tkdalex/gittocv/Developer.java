package com.tkdalex.gittocv;

import java.util.HashMap;

public class Developer { // Should be committer
	public String name;
	public String email;
	public int commit;
	
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
	}
	
	public void initSocialInfo(String socialname) {
		GitSocialScraper socialscraper = new GitSocialScraper(socialname);
		try {
			socialscraper.getInfo(this.email);
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
		
	}
}
