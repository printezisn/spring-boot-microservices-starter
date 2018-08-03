package com.printezisn.moviestore.accountservice.account.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.printezisn.moviestore.accountservice.account.entities.Account;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.mappers.InstantMapper;
import com.printezisn.moviestore.common.mappers.UUIDMapper;

/**
 * Mapper class for Account objects
 */
@Mapper(componentModel = "spring", uses = { UUIDMapper.class, InstantMapper.class })
public interface AccountMapper {

    /**
     * Converts an Account object to AccountDto
     * 
     * @param account
     *            The Account object
     * @return The converted AccountDto object
     */
    @Mappings({
        @Mapping(source = "password", target = "password", ignore = true),
        @Mapping(source = "passwordSalt", target = "passwordSalt", ignore = true)
    })
    AccountDto accountToAccountDto(final Account account);

    /**
     * Converts an AccountDto object to Account
     * 
     * @param accountDto
     *            The AccountDto object
     * @return The converted Account object
     */
    Account accountDtoToAccount(final AccountDto accountDto);
}
