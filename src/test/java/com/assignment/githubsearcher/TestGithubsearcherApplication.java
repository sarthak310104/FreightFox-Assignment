package com.assignment.githubsearcher;

import org.springframework.boot.SpringApplication;

public class TestGithubsearcherApplication {

	public static void main(String[] args) {
		SpringApplication.from(GithubsearcherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
