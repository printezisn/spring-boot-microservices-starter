package com.printezisn.moviestore.common.models.account;

import java.util.List;

import com.printezisn.moviestore.common.dto.account.AccountDto;

import lombok.Data;

/**
 * The model for account results
 */
@Data
public class AccountResultModel {
	private AccountDto result;
	private List<String> errors;
}
