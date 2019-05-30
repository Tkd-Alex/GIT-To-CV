package com.tkdalex.gittocv;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class DevelopersVisitor implements CommitVisitor {

	private List<String> getImportPy(String line) {
		String[] splitted = line.split("import");
		String imports = splitted[ splitted.length-1 ];
		String[] allImports = imports.split(",");
		List<String> importsTrimmed = Arrays.asList(allImports).stream()
				.map(x -> x.trim())
				.collect(Collectors.toList()); 
		return importsTrimmed;
	}
	
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		for(Modification modification : commit.getModifications()) {
			List<String> lines = Arrays.asList( modification.getDiff().split("\n") );
			List<String> additions = lines.stream()
					.filter( x -> !x.startsWith("++") && x.startsWith("+") )
					.map(x -> x.replace("+","").replace("\n"," ").trim())
					.collect(Collectors.toList()); 
			
			for(String addition : additions) {
				if(addition.contains("import")) {
					System.out.println(getImportPy(addition));
				}
					
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

}
