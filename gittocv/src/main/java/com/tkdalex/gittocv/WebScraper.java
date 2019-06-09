package com.tkdalex.gittocv;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebScraper {
	
	public String baseurl;
	
	public WebScraper(String extension) {
		if(extension.equals("js")) this.baseurl = "https://www.npmjs.com/package/";
		else this.baseurl = ""; // tmp
	}
	
	public String classifyImport(String packagename) {
		try {
			Document doc = Jsoup.connect(this.baseurl + packagename).get();
			String readme = doc.getElementById("readme").text();
			if(readme.toLowerCase().contains("node.js")) return "backend";
			return "frontend";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
