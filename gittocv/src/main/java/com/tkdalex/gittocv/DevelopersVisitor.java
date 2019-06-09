package com.tkdalex.gittocv;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class DevelopersVisitor implements CommitVisitor {

	private static final String HTML_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
	private static Pattern pattern = Pattern.compile(HTML_PATTERN);
	
	private HashMap<String, Developer> developers;
	private HashMap<String, String[]> fileExstensions;
	
	public DevelopersVisitor() {
		this.developers = new HashMap<String, Developer>();
		this.fileExstensions = new HashMap<String, String[]>();
		
		this.fileExstensions.put( "backend", new String[] { "sh", "py", "c", "cpp" } );
		this.fileExstensions.put( "frontend", new String[] { "css", "scss", "html", "ts", "ui" } );
		this.fileExstensions.put( "writer", new String[] { "pdf", "md", "txt", "tex" } ); 
		this.fileExstensions.put( "undefined", new String[] { "php", "java", "js" } ); // py
	}
	
	public HashMap<String, Developer> getDevelopers(){
		return this.developers;
	}
		
	private List<String> getImportPYTHON(String line) {
		String[] splitted = line.split("import");
		String imports = splitted[ splitted.length-1 ];
		String[] allImports = imports.split(",");
		List<String> importsTrimmed = Arrays.asList(allImports).stream()
				.map(x -> x.trim())
				.collect(Collectors.toList()); 
		return importsTrimmed;
	}
	
	private static boolean checkIfFileHasExtension(String s, String[] extn) {
	    return Arrays.stream(extn).anyMatch(entry -> s.endsWith(entry));
	}
	
	private static boolean hasHTMLTags(String text){
	    Matcher matcher = pattern.matcher(text);
	    return matcher.find();
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
		for(String line : lines) { 
			if(line.contains("import")) { // import java.awt.BorderLayout;
				line = line.replace("import", "").replace(";", "").trim(); // java.awt.BorderLayout;
				System.out.println(line);
				// imports.add( splitted[0] + "." + splitted[1] ); 
			}
		}
		return imports;
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
			if( (line.contains("import") || line.contains("require")) && !line.contains("/") ) { // skip custom own module with /
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
					dev.editPoints(i, 1);
			}else {
				for(String ext : this.fileExstensions.get(i)) {
					if ( modification.getFileName().endsWith(ext) ) {
						if(ext.equals("php")) {
							if(hasHTMLTags(modification.getSourceCode())) dev.editPoints("frontend", 1);
							else dev.editPoints("backend", 1);
						}
						else {						
							List<String> additions = getOnlyAdditions(modification);
							if(ext.equals("java")) { // import java.awt.BorderLayout;
								// System.out.println( getImportJAVA(additions) );
								getImportsJAVA(additions);
							}
							else if(ext.equals("js")) {
								WebScraper webscraper = new WebScraper("js");
								List<String> imports = getImportsJAVASCRIPT(additions) ;
								
								for(String imp : imports) {
									String type = webscraper.classifyImport(imp);
									if(!type.equals(null)) { 
										dev.editPoints(type, 1);
										break; // Other points are linked by file extension. 1File => 1Point, break the loop (limit request)
									}
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
		if(!contains) developers.put(commit.getAuthor().getName(), new Developer( commit.getAuthor().getName(), commit.getAuthor().getEmail() ));
		Developer dev = developers.get(commit.getAuthor().getName());
		dev.commit++;
		
		for(Modification modification : commit.getModifications()) {			
			this.updatePoint(dev, modification);
			
			writer.write(
					commit.getHash(),
					commit.getAuthor().getName(),
					commit.getCommitter().getName(),
					modification.getFileName(),
					modification.getType(),
					modification.getAdded()
			);
		}
	}

}
