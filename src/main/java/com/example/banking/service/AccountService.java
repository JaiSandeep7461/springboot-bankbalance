package com.example.banking.service;

import com.example.banking.dto.AccountDto;

public interface AccountService {

    AccountDto createAccount(AccountDto accountDto);

    AccountDto getAccountById(Long id);

    AccountDto deposit(Long id,double amount);

    AccountDto withDraw(Long id,double amount);
}
