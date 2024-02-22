package com.loorey.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.loorey.movie")
public class FlixNestApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlixNestApplication.class, args);
	}

}
