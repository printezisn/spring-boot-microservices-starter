package com.printezisn.moviestore.accountservice.integ.models;

import java.util.List;

import com.printezisn.moviestore.accountservice.account.dto.AccountDto;

import lombok.Data;

/**
 * The model for results coming from the account controller
 */
@Data
public class AccountResultModel {
	private AccountDto result;
	private List<String> errors;
}
