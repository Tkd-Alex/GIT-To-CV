package com.tkdalex.gittocv;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class DevelopersVisitor implements CommitVisitor {

	private static final String HTML_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
	private static Pattern pattern = Pattern.compile(HTML_PATTERN);
	
	private Properties properties = new Properties();
	
	private HashMap<String, Developer> developers;
	private HashMap<String, String[]> fileExstensions;
	private List<String> java_fe;
	
	public DevelopersVisitor( Properties properties ) {
		this.properties = properties;
		this.developers = new HashMap<String, Developer>();
		this.fileExstensions = new HashMap<String, String[]>();
		
		this.java_fe = convertToLowerCase( Arrays.asList( properties.getProperty("java_fe").split(";") ) );
		
		for(String type : new String[] { "backend", "frontend", "writer", "undefined" } ) {
			List<String> exstensions = convertToLowerCase( Arrays.asList( properties.getProperty( type ).split(";") ) );
			String[] exstensionsArray = new String[exstensions.size()];
			this.fileExstensions.put( type , exstensions.toArray(exstensionsArray) );
		}
	}
	
	public HashMap<String, Developer> getDevelopers(){
		return this.developers;
	}
	
	private static boolean checkIfFileHasExtension(String s, String[] extn) {
	    return Arrays.stream(extn).anyMatch(entry -> s.endsWith(entry));
	}
	
	private static boolean hasHTMLTags(String text){
	    Matcher matcher = pattern.matcher(text);
	    return matcher.find();
	}
	
	private static List<String> convertToLowerCase(List<String> strings) {
		return strings.stream() 
				.map( x -> x.toLowerCase().trim()) 
				.collect(Collectors.toList());
	}
	
	private static List<String> getOnlyAdditions(Modification modification){
		List<String> lines = Arrays.asList( modification.getDiff().split("\n") );
		List<String> additions = lines.stream()
				.filter( x -> !x.startsWith("++") && x.startsWith("+") )
				.map(x -> x.replace("+","").replace("\n"," ").trim())
				.collect(Collectors.toList()); 
		return additions;
	}
	
	private static List<String> getImportsJAVA(List<String> lines){
		List<String> imports = new ArrayList<>();
		for(String line : lines) 
			if(line.startsWith("import") && line.split("\\.").length > 1) // import java.awt.BorderLayout;
				imports.add( (line.split("\\.")[0].replace("import",  "") + "." + line.split("\\.")[1] ).replace(";+$", "").trim() ) ;

		LinkedHashSet<String> hashSet = new LinkedHashSet<>(imports);
		return new ArrayList<String>(hashSet);
	}
	
	// https://developer.mozilla.org/it/docs/Web/JavaScript/Reference/Statements/import
	/*
	import defaultExport from "module-name";
	import * as name from "module-name";
	import { export } from "module-name";
	import { export as alias } from "module-name";
	import { export1 , export2 } from "module-name";
	import { export1 , export2 as alias2 , [...] } from "module-name";
	import defaultExport, { export [ , [...] ] } from "module-name";
	import defaultExport, * as name from "module-name";
	import "module-name";
	*/
	private static List<String> getImportsJAVASCRIPT(List<String> lines){
		List<String> imports = new ArrayList<>();
		for(String line : lines) { 
			// if( (line.contains("import") || line.contains("require")) && !line.contains("/") ) { // skip custom own module with /
			if( (line.trim().startsWith("import") || line.trim().startsWith("require")) && !line.contains("/") ) {
				Pattern p = Pattern.compile("\"([^\"]*)\"");
				Matcher m = p.matcher(line.replace("'", "\""));
				while (m.find()) imports.add(m.group(1));
			}
		}
		return imports;
	}
	
	private void updatePoint(Developer dev, Modification modification) {
		for (String i : this.fileExstensions.keySet()) {
			if (!i.equals("undefined")) {
				if( checkIfFileHasExtension(modification.getFileName(), this.fileExstensions.get(i)) ) 
					dev.editPoints(i, ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
			}else {
				for(String ext : this.fileExstensions.get(i)) {
					if ( modification.getFileName().endsWith(ext) ) {
						if(ext.equals("php")) {
							if(hasHTMLTags(modification.getSourceCode())) dev.editPoints("frontend", ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
							else dev.editPoints("backend", ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
						}
						else if ( ext.equals("java") || ext.equals("js") ){						
							List<String> imports = new ArrayList<>();
							List<String> additions = getOnlyAdditions(modification);
							WebScraper webscraper = new WebScraper("js");
							
							if(ext.equals("java")) imports = getImportsJAVA(additions);
							else imports = getImportsJAVASCRIPT(additions);
							
							/*
							 * That's the point. For 3 category the code work as well. Rewrite for multiple category.
							boolean hasBackendLib = false;
							
							for(String imp : imports) {
								String type = null; 
								if(ext.equals("java")) {
									if( dev.extraCategory.size() == 3) type = this.java_fe.contains(imp.toLowerCase()) ? "frontend" : "backend";
									else { // Check implementations for each category.
										for(String cat : dev.extraCategory) {
											List<String> currentPackages = convertToLowerCase( Arrays.asList( this.properties.getProperty(cat).split(";") ) );
											if( currentPackages .contains( imp.toLowerCase() ) ) type = cat;
										}
									}	
								}
								else type = webscraper.classifyImport(imp);

								if(!type.equals(null) && type.equals("backend")) { 
									hasBackendLib = true;
									break; // Other points are linked by file extension. 1File => 1Point, break the loop (limit request)
								}
							}
								
							if(hasBackendLib == false) dev.editPoints("frontend", ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
							else dev.editPoints("backend", ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
							*/
							
							boolean hasBackendLib = false;
							HashMap<String, Integer> categoryFile = new HashMap();
							List<String> packageIdentified = new ArrayList<String>();
							categoryFile.put("backend", 0);
							// System.out.println(imports);
							
							for(String imp: imports) {
								if(ext.equals("java")) {
									// The properties file have extraCategory?
									if( dev.extraCategory.size() == 0 ) { // Default category 
										String type = this.java_fe.contains(imp.toLowerCase()) ? "frontend" : "backend";
										if(!type.equals(null) && type.equals("backend")) { 
											hasBackendLib = true;
											break; // Other points are linked by file extension. 1File => 1Point, break the loop (limit request)
										}
									}else {
										for (String cat : dev.getKeyPoints()){
											List<String> currentPackages = new ArrayList<String>();
											if(cat.equals("frontend")) currentPackages = this.java_fe;
											else if (!cat.equals("frontend")) currentPackages = convertToLowerCase( Arrays.asList( this.properties.getProperty(cat).split(";") ) );
											
											if(!cat.equals("backend")){
												if( currentPackages .contains( imp.toLowerCase() ) ) {
													if(categoryFile.get(cat) == null) categoryFile.put(cat, 0); 
													categoryFile.put(cat, categoryFile.get(cat) + 1);
													packageIdentified.add(imp);
												}
											}
										}
										if (!packageIdentified.contains(imp)) categoryFile.put("backend", categoryFile.get("backend") + 1);
									}
								}
								else {
									String type = webscraper.classifyImport(imp);
									if(!type.equals(null) && type.equals("backend")) { 
										hasBackendLib = true;
										break; // Other points are linked by file extension. 1File => 1Point, break the loop (limit request)
									}
								}
							}
							
							if(ext.equals("js") || dev.extraCategory.size() == 0) { // Standard method for js file or java with default category
								if(hasBackendLib == false) dev.editPoints("frontend", ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
								else dev.editPoints("backend", ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
							}
							else {
								/*
								 * Just do it!
								 * If we have extraCategory get the max value of categoryFile, for example
								 * { android: 5, database: 2, other: 1 } , max = 5
								 * Assign 1 point * modification (example: 1000) at category android 
								 * For category with same point
								 * { android: 5, database: 5, other: 1 } , max = 5
								 * Assign 1 point * modification/same_category (example: 1000/2) at category android and database 
								 */
								int max = Collections.max(categoryFile.values());
								List<String> maxKeys = categoryFile.entrySet().stream()
							    	.filter(entry -> entry.getValue() == max)
							    	.map(entry -> entry.getKey())
							    	.collect(Collectors.toList());
								
								if(maxKeys.size() == 1) dev.editPoints(maxKeys.get(0), ((modification.getAdded() == 0) ? 1 : modification.getAdded()));
								else {
									for(String cat : maxKeys) dev.editPoints(cat, (( modification.getAdded() == 0) ? 1 : (Integer)(modification.getAdded() / maxKeys.size()) ));
								}
							}
								
						}
					}
				}
			}
		}
	}

	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		// Check if the HashMap contains already the developer (by name), else create a new instance of Developer.
		boolean contains = Arrays.stream(developers.keySet().toArray()).anyMatch(commit.getAuthor().getName()::equals);
		if(!contains) {
			Developer newDev = new Developer( commit.getAuthor().getName(), commit.getAuthor().getEmail() );
			newDev.initExtraCategory(this.properties);
			developers.put(commit.getAuthor().getName(), newDev );
		}
		Developer dev = developers.get(commit.getAuthor().getName());
		dev.commit++;
		
		for(Modification modification : commit.getModifications()) {	
			// System.out.println( commit.getHash() );
			// System.out.println(commit.getMsg() );
			this.updatePoint(dev, modification);
		}
	
	}

}
