package com.printezisn.moviestore.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class of the application
 */
@SpringBootApplication(scanBasePackages = { "com.printezisn.moviestore.accountservice", "com.printezisn.moviestore.common" })
public class AccountServiceApplication {

	/**
	 * The main method of the application
	 * @param args The command-line arguments
	 */
	public static void main(final String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}
}
