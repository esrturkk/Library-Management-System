package com.backend.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import io.github.cdimascio.dotenv.Dotenv;
@EntityScan(basePackages = {"com.backend"})
@SpringBootApplication
public class LibraryApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load(); // .env dosyasını yükler
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		SpringApplication.run(LibraryApplication.class, args);
	}

}
