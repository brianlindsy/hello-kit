package com.brianlindsey.SlackNewsletter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SlackNewsletterApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlackNewsletterApplication.class, args);
	}

}
