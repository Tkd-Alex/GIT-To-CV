package com.tkdalex.gittocv;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class DevelopersVisitor implements CommitVisitor {

	private HashMap<String, Developer> developers;
	private HashMap<String, String[]> fileExstensions;
	
	public DevelopersVisitor() {
		this.developers = new HashMap<String, Developer>();
		this.fileExstensions = new HashMap<String, String[]>();
		
		this.fileExstensions.put( "backend", new String[] { "sh", "py", "c", "cpp" } );
		this.fileExstensions.put( "frontend", new String[] { "css", "scss", "html", "ts", "ui" } );
		this.fileExstensions.put( "writer", new String[] { "pdf", "md", "txt" } );
		this.fileExstensions.put( "undefined", new String[] { "php", "java", "js" } );
	}
	
	public HashMap<String, Developer> getDevelopers(){
		return this.developers;
	}
		
	private List<String> getImportPy(String line) {
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
	
	private void updatePoint(Developer dev, Modification modification) {
		for (String i : this.fileExstensions.keySet()) {
			if (!i.equals("undefined"))
				if( checkIfFileHasExtension(modification.getFileName(), this.fileExstensions.get(i)) ) 
					dev.editPoints(i, 1);
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
