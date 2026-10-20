package com.assignment.githubsearcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GithubsearcherApplication {
	public static void main(String[] args) {
		SpringApplication.run(GithubsearcherApplication.class, args);
	}
}