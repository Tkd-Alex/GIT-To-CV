package com.tkdalex.gittocv;

import java.util.HashMap;

public class Developer {
	public String name;
	public String email;
	public int commit;
	
	private HashMap<String, Integer> devPoints;
	
	public Developer(String name, String email) {
		this.name = name;
		this.email = email;
		this.commit = 0;
		this.devPoints = new HashMap<String, Integer>();
		this.devPoints.put("backend", 0);
		this.devPoints.put("frontend", 0);
		this.devPoints.put("writer", 0);
	}
	
	public Integer editPoints(String key, Integer value) {
		this.devPoints.put(key, this.devPoints.get(key) + value);
		return this.devPoints.get(key);
	}
	
	public void print() {
		System.out.println("Name: " + this.name);
		System.out.println("Email: " + this.email);
		System.out.println("Commit: " + this.commit);
		for (String i : this.devPoints.keySet()) {
			System.out.println("Category: " + i + " Points: " + this.devPoints.get(i));
		}
	}
}