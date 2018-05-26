package com.printezisn.moviestore.accountservice.account.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.printezisn.moviestore.accountservice.account.entities.Account;

/**
 * The repository layer for the accounts
 */
@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
	
	/**
	 * Searches for an account based on its email address
	 * 
	 * @param emailAddress The email address of the account
	 * @return The account
	 */
	Optional<Account> findByEmailAddress(final String emailAddress);
}
