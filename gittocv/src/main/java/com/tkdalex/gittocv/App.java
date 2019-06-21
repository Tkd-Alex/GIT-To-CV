package com.tkdalex.gittocv;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		
		HashMap<String, Developer> developers = analyzer.getDevelopers();
		for (Developer i : developers.values()) {
			i.print();
			System.out.println("=====================================");
		}
	}
}
