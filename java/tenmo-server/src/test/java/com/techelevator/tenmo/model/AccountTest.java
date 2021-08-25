package com.techelevator.tenmo.model;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    public void add_and_subtract_account_tests() {
        Account account = new Account(100, 100, new BigDecimal("1000.00"));
        account.addAmount(new BigDecimal("100.00"));
        Assert.assertTrue(new BigDecimal("1100.00").equals(account.getBalance()));
        account.subtractAmount(new BigDecimal("100.00"));
        Assert.assertTrue(new BigDecimal("1000.00").equals(account.getBalance()));
    }

}