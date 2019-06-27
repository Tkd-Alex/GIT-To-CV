package com.tkdalex.gittocv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

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
        for(String key : new String[] { "repository", "backend", "frontend", "writer", "undefined", "java_fe", "export_as" } ) {
			String singleProp = properties.getProperty( key );
			if(singleProp == null || singleProp.equals("") ) isValid = false;
			else if( arrayProps.contains( key ) && singleProp.split(";").length == 0 ) isValid = false;
			
			if(isValid == false) System.out.println("Please insert a valid value for " + key + " in config.properties file.");
		}
		return isValid;
	}
	
	private static String getRepoName(String url) {
		String[] splitted = url.split("/");
		return splitted[ splitted.length-1 ];
	}
	
	private void zipFolder(Path sourceFolderPath, Path zipPath) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
                Files.copy(file, zos);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
        zos.close();
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

		//HashMap<String, Developer> developers = analyzer.getDevelopers();
		HashMap<String, Developer> orginalDev = analyzer.getDevelopers();
		
		// Removing duplicate users. Primary key = email
		HashMap<String, Developer> developers = new HashMap<String, Developer>();
		for(Developer i : orginalDev.values()) {
			Developer dev = null;
			for(Developer j : developers.values()) if( j.email.equals(i.email) ) dev = j;
			if(dev == null ) developers.put(i.name, i);
			else {
				System.out.println("Merge points for: " + i.name + " , " + dev.name );
				
				// System.out.println("Backend: " + i.getPoints("backend") + " + " + dev.getPoints("backend") );
				dev.editPoints("backend", i.getPoints("backend")) ;
				
				// System.out.println("Frontend: " + i.getPoints("frontend") + " + " + dev.getPoints("frontend") );
				dev.editPoints("frontend", i.getPoints("frontend")) ;
				
				// System.out.println("Writer: " + i.getPoints("writer") + " + " + dev.getPoints("writer") );
				dev.editPoints("writer", i.getPoints("writer")) ;
			}
		}
		
		System.out.println("Start scraping social info for: " + developers.size() + " developers.");
		Integer max_commit = 0;
		for (Developer i : developers.values()) {
			if(i.commit > max_commit) max_commit = i.commit;
			i.initSocialInfo(socialname);
		}
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String filename =  getRepoName(urlOrPath) + "_" + timestamp;
		
		if(properties.getProperty("export_as").equals("csv")) {
			try {
				PrintWriter writer = new PrintWriter(filename + ".csv", "UTF-8");
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
		else if(properties.getProperty("export_as").equals("html")) {
			JSONArray outputData = new JSONArray();
			for (Developer i : developers.values()) {
				JSONObject singleDeveloper = new JSONObject();
				Integer total = i.getPoints("backend") + i.getPoints("frontend") + i.getPoints("writer"); 
				singleDeveloper.put("name", i.name );
				singleDeveloper.put("email", i.email );
				singleDeveloper.put("id", ( (i.getId() == null) ?  " " : i.getId() ) );
				singleDeveloper.put("username", ( (i.getUsername() == null) ?  " " : i.getUsername() ) );
				singleDeveloper.put("avatar_url", ( (i.getAvatar_url() == null) ?  " " : i.getAvatar_url() ) );
				singleDeveloper.put("website", ( (i.getWebsite() == null) ?  " " : i.getWebsite() ) );
				singleDeveloper.put("location", ( (i.getLocation() == null) ?  " " : i.getLocation() ) );
				singleDeveloper.put("bio", ( (i.getBio() == null) ?  " " : i.getBio() ) );
				singleDeveloper.put("created_at", ( (i.getCreated_at() == null) ?  " " : i.getCreated_at() ) );
				singleDeveloper.put("commit", i.commit );
				singleDeveloper.put("commit_star", Math.floor( ((float)i.commit * 5 ) / max_commit ) );
				singleDeveloper.put("backend", Math.round( ((float)i.getPoints("backend")*100)/total ) );
				singleDeveloper.put("frontend", Math.round( ((float)i.getPoints("frontend")*100)/total ) );
				singleDeveloper.put("writer", Math.round( ((float)i.getPoints("writer")*100)/total ) );
				outputData.put(singleDeveloper);
			}
			try (FileWriter file = new FileWriter("html/js/git_data.js")) {
	            file.write("data = " + outputData.toString());
	            file.flush();
	            
	            zipFolder(Paths.get("html"), Paths.get("./" + filename + ".zip" ));
	        } catch (IOException e) { e.printStackTrace(); } catch (Exception e) {
				e.printStackTrace();
			}
		}
		else System.out.println("Not valid output provided. Output supported: csv or html");
		
		
	}
}
