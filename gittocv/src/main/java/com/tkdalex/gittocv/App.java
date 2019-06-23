package com.tkdalex.gittocv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.NoPersistence;
import org.repodriller.scm.GitRemoteRepository;
import org.repodriller.scm.GitRepository;

public class App implements Study
{
	
	private static Properties properties = new Properties();
	
	public static void main(String[] args) {
		properties = loadProperties();
		if(validateProperties(properties) == false) return;
		
		new RepoDriller().start(new App());
	}
	
	private static Properties loadProperties() {
		Properties properties = new Properties();
		InputStream input = null;

	    try {
	        input = new FileInputStream("config.properties");
	        properties.load(input);
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return properties;
	}

	private static boolean isRemote(String url) {
		if(url.startsWith("www") || url.startsWith("http") ) return true;
		return false;
	}
	
	private static String getSocialName(String url) {
		if(isRemote(url)) {
			String urlNoProtocol = url.replace("www", "").replace("https://", "").replace("http://", "");
			String[] splitted = urlNoProtocol.split("\\.");
			if(splitted.length > 0) return splitted[0].trim();
		}else {
			File file = new File( url + "/.git/config" );
			BufferedReader reader = null;

			try {
			    reader = new BufferedReader(new FileReader(file));
			    String text = null;

			    while ((text = reader.readLine()) != null) {
			        if(text.trim().startsWith( "url = " )) {
			        	String urlNoProtocol = text
			        			.replace("url", "")
			        			.replace("=", "")
			        			.replace("git@", "")
			        			.replace("www", "")
			        			.replace("https://", "")
			        			.replace("http://", "");
	        			String[] splitted = urlNoProtocol.split("\\.");
	        			if(splitted.length > 0) return splitted[0].trim();
			        }
			    }
			    
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    try {
			        if (reader != null) { reader.close(); }
			    } catch (IOException e) { e.printStackTrace(); }
			}
		}
		return "";
	}
	
	private static boolean validateProperties( Properties properties ) {
		boolean isValid = true;
		List<String> arrayProps =  new ArrayList<String>() { 
            { 
                add("backend"); 
                add("frontend"); 
                add("writer"); 
                add("undefined");
            } 
		};
        for(String key : new String[] { "repository", "backend", "frontend", "writer", "undefined", "java_fe" } ) {
			String singleProp = properties.getProperty( key );
			if(singleProp == null || singleProp.equals("") ) isValid = false;
			else if( arrayProps.contains( key ) && singleProp.split(";").length == 0 ) isValid = false;
			
			if(isValid == false) System.out.println("Please insert a valid value for " + key + " in config.properties file.");
		}
		return isValid;
	}
	
	public void execute() {
		DevelopersVisitor analyzer = new DevelopersVisitor(properties);
		String urlOrPath = properties.getProperty("repository");
		
		if(isRemote(urlOrPath) == true)
			new RepositoryMining()
				.in(GitRemoteRepository.hostedOn(urlOrPath).buildAsSCMRepository())
				.through(Commits.all())
				.withThreads()
				.filters(
					new OnlyNoMerge()
				)
				.process(analyzer, new NoPersistence())
				.mine();
		else
			new RepositoryMining()
				.in(GitRepository.singleProject(urlOrPath))
				.through(Commits.all())
				.withThreads()
				.filters(
					new OnlyNoMerge()
				)
				.process(analyzer, new NoPersistence())
				.mine();
		

		String socialname = getSocialName(urlOrPath);

		HashMap<String, Developer> developers = analyzer.getDevelopers();
		for (Developer i : developers.values()) i.initSocialInfo(socialname);
		
		if(properties.getProperty("export_as").equals("csv")) {
			try {
				PrintWriter writer = new PrintWriter("the-file-name.csv", "UTF-8");
				writer.println("Name;Email;SocialID;SocialUsername;AvatarURL;WebSite;Location;Bio;CreatedAt;Commits;Backend%;Frontend%;Writer%");
				for (Developer i : developers.values()) {
					Integer total = i.getPoints("backend") + i.getPoints("frontend") + i.getPoints("writer"); 
					String singleline = 
							i.name + ";" + 
							i.email + ";" +
							( (i.getId() == null) ?  " " : i.getId() ) + ";" + 
							( (i.getUsername() == null) ?  " " : i.getUsername() ) + ";" + 
							( (i.getAvatar_url() == null) ?  " " : i.getAvatar_url() ) + ";" + 
							( (i.getWebsite() == null) ?  " " : i.getWebsite() ) + ";" + 
							( (i.getLocation() == null) ?  " " : i.getLocation() ) + ";" + 
							( (i.getBio() == null) ?  " " : i.getBio() ) + ";" + 
							( (i.getCreated_at() == null) ?  " " : i.getCreated_at() ) + ";" + 
							i.commit + ";" +
							Math.round( ((float)i.getPoints("backend")*100)/total )  + ";" +
							Math.round( ((float)i.getPoints("frontend")*100)/total )  + ";" +
							Math.round( ((float)i.getPoints("writer")*100)/total )  + ";"
							;
					writer.println(singleline);
				}
				writer.close();
				
			} catch (FileNotFoundException e) { e.printStackTrace();
			} catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		}
		
		
		
	}
}
