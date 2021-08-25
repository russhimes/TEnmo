package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    public BigDecimal getBalanceByAccountId(long id);

    public BigDecimal getBalanceByUserName(String username);

    public Account getAccountById(long id);

    public long getAccountIdByUserId(long userId);

    public Account getAccountByUserName(String userName);

    public void updateAccountBalances(long fromAccountId, long toAccountId, BigDecimal amount);

    //public Transfer getTransfer();
    //public void transfer???;
}
