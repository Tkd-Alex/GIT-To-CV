package com.tkdalex.gittocv;

import java.sql.Timestamp;
import java.util.HashMap;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;
import org.repodriller.scm.GitRepository;

public class App implements Study
{
	public static void main(String[] args) {
		new RepoDriller().start(new App());
	}

	private static String getRepoName(String url) {
		String[] splitted = url.split("/");
		return splitted[ splitted.length-1 ];
	}
	
	public void execute() {
		DevelopersVisitor analyzer = new DevelopersVisitor();
		String urlOrPath = "/home/alessandro/GitHub/Giannetto-Mobile";
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String csvfilename = "./" + getRepoName(urlOrPath) + "_" + timestamp +".csv";
		CSVFile csvfile = new CSVFile(csvfilename);
		
		/*
		new RepositoryMining()
			.in(GitRemoteRepository.hostedOn(urlOrPath).buildAsSCMRepository())
			.through(Commits.all())
			.withThreads()
			.filters(
				new OnlyNoMerge()
			)
			.process(analyzer, csvfile)
			.mine();
		*/
		
		new RepositoryMining()
			.in(GitRepository.singleProject(urlOrPath))
			.through(Commits.all())
			.withThreads()
			.filters(
				new OnlyNoMerge()
			)
			.process(analyzer, csvfile)
			.mine();
		
		HashMap<String, Developer> developers = analyzer.getDevelopers();
		for (Developer i : developers.values()) {
			i.print();
			System.out.println("=====================================");
		}
	}
}
