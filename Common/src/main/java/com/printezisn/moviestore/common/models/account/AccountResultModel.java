package com.printezisn.moviestore.common.models.account;

import java.util.LinkedList;
import java.util.List;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.Result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class that holds the result of an account service call
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResultModel implements Result<AccountDto> {

    private AccountDto result;

    @Builder.Default
    private List<String> errors = new LinkedList<>();
}
