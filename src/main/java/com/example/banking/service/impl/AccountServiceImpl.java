package com.example.banking.service.impl;

import com.example.banking.dto.AccountDto;
import com.example.banking.dto.TransferFundDto;
import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.exception.AccountException;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.repository.AccountRepository;
import com.example.banking.service.AccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    private TransactionRepository transactionRepository;

    private static final String TRANSACTION_TYPE_DEPOSIT="DEPOSIT";

    public AccountServiceImpl(AccountRepository accountRepository,TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto);

        return AccountMapper.mapToAccountDto(accountRepository.save(account));
    }

    @Override
    public AccountDto getAccountById(Long id) {
       Account account= accountRepository.findById(id).orElseThrow(()-> new AccountException("Account does not exist"));
        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    public AccountDto deposit(Long id, double amount) {
        Account account= accountRepository.findById(id).orElseThrow(()-> new AccountException("Account does not exist"));
        double total = account.getBalance() + amount;
        account.setBalance(total);
      Account savedAccount =  accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccountId(account.getId());
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE_DEPOSIT);
        transaction.setTimeStamp(LocalDateTime.now());
        transactionRepository.save(transaction);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto withDraw(Long id, double amount) {
        Account account = accountRepository.findById(id).orElseThrow(()-> new AccountException("Account does not exist"));
        if(account.getBalance()<amount){
            throw new RuntimeException("InSufficient Amount");
        }

        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
       return accounts.stream().map((account)-> AccountMapper.mapToAccountDto(account))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(()-> new AccountException("Account does not exist"));

        accountRepository.deleteById(id);
    }

    @Override
    public void transferFunds(TransferFundDto transferFundDto) {

        //Retrive the account from which we send the amount
       Account fromAccount = accountRepository.findById(transferFundDto.fromAccountId())
                .orElseThrow(()-> new AccountException("Account does not exists"));

       Account toAccount = accountRepository.findById(transferFundDto.toAccountId())
               .orElseThrow(()-> new AccountException("Account doesnot exist"));

       fromAccount.setBalance(fromAccount.getBalance()- transferFundDto.amount());

       toAccount.setBalance(toAccount.getBalance()+ transferFundDto.amount());

       accountRepository.save(fromAccount);

       accountRepository.save(toAccount);
        }
}
