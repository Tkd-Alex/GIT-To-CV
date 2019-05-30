package com.tkdalex.gittocv;

import java.sql.Timestamp;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;
import org.repodriller.scm.GitRepository;

public class App implements Study
{
	public static void main(String[] args) {
		new RepoDriller().start(new App());
	}

	private String getRepoName(String url) {
		String[] splitted = url.split("/");
		return splitted[ splitted.length-1 ];
	}
	
	public void execute() {
		DevelopersVisitor analyzer = new DevelopersVisitor();
		String url = "https://github.com/Tkd-Alex/Telegram-InstaPy-Scheduling";
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String csvfilename = "./" + getRepoName(url) + "_" + timestamp +".csv";
		CSVFile csvfile = new CSVFile(csvfilename);
		new RepositoryMining()
			.in(GitRemoteRepository.hostedOn(url)
			.buildAsSCMRepository()).through(Commits.all())
			.withThreads()
			.process(analyzer, csvfile)
			.mine();
	}
}
